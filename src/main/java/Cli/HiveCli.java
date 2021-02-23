package Cli;

import DbManager.Oracle.OracleManager;
import Downloader.Reader.BlockingQueue.Impl.HiveReader;
import Downloader.Reader.BlockingQueue.Impl.OracleParallelReader;
import Downloader.Reader.BlockingQueue.Reader;
import Downloader.Reader.ReaderType;
import Downloader.Writer.BlockingQueue.Impl.CSVWriter;
import Downloader.Writer.BlockingQueue.Impl.ParquetWriter;
import Downloader.Writer.BlockingQueue.Writer;
import Downloader.Writer.FileType;
import Queue.BlockingQueue.Impl.CSVQueueManager;
import Queue.BlockingQueue.Impl.ParquetQueueManager;
import Queue.BlockingQueue.QueueManager;
import avro.Impl.OracleTransformer;

import java.io.File;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static picocli.CommandLine.*;

/**
 * 멀티 스레딩 fetch 커맨드 클래스
 * 하이브
 */
@Command(name = "hive")
public class HiveCli implements Callable<Integer> {

    @ParentCommand
    private TransferCli transferCli;

    @Option(names = {"-c", "--thread-count"}, defaultValue = "4")
    private int threadCount;

    @Option(names = {"-type", "--file-type"}, description = "Write File Type (CSV, PARQUET)", required = true)
    private FileType fileType;

    @Option(names = {"-reader", "--reader-type"}, description = "Fetch Reader Type (HIVE, ORACLE)", required = true)
    private ReaderType readerType;

    @Option(names = {"-s", "--fetch-size"}, defaultValue = "10000")
    private int fetchSize;

    @Option(names = {"-sql", "--execute-sql"}, required = true)
    private File executeSqlFile;

    @Option(names = {"-host", "--host-name"}, required = true)
    private String hostName;

    @Option(names = {"-u", "--user-name"}, required = true)
    private String userName;

    @Option(names = {"-p", "--password"}, required = true)
    private String password;


    @Override
    public Integer call() throws Exception {

        String executeSql = Files.readString(executeSqlFile.toPath());
        String outputFileName = transferCli.getOutputFileName();

        final int totalRowsCount = getTotalRowsCount(executeSql);

        final ExecutorService readerPool = Executors.newFixedThreadPool(threadCount);
        final ExecutorService writerPool = Executors.newFixedThreadPool(threadCount);

        final int chunkSize = getChunkSize(totalRowsCount);

        List<QueueManager<?>> queueManagerList = createQueueManagers(fileType, threadCount);

        List<CompletableFuture<Void>> readerList = createReaders(executeSql, outputFileName, readerPool, chunkSize, readerType, queueManagerList);
        List<CompletableFuture<Void>> writerList = createWriters(outputFileName, writerPool, fileType, queueManagerList);

        final CompletableFuture<Void> readerAll = CompletableFuture.allOf(readerList.toArray(CompletableFuture[]::new)).thenRunAsync(() -> {
            System.out.println("reader 완료");
        });
        final CompletableFuture<Void> writerAll = CompletableFuture.allOf(writerList.toArray(CompletableFuture[]::new)).thenRunAsync(() -> {
            System.out.println("writer 완료");
        });

        readerAll.join();
        writerAll.cancel(true);

        return 0;
    }

    private int getChunkSize(double totalRowsCount) {
        return (int) Math.ceil(totalRowsCount / threadCount);
    }

    private List<QueueManager<?>> createQueueManagers(FileType fileType, int threadCount) {

        List<QueueManager<?>> list = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            switch (fileType) {
                case CSV -> {
                    list.add(new CSVQueueManager());
                }
                case PARQUET -> {
                    list.add(new ParquetQueueManager(new OracleTransformer(), "jong2", "com.jong2"));
                }
                case DISRUPTOR_CSV -> {
                }
            }
        }
        return list;
    }

    private List<CompletableFuture<Void>> createReaders(String executeSql, String outputFileName, ExecutorService readerPool, int chunkSize, ReaderType readerType, List<QueueManager<?>> queueManagerList) throws SQLException {
        int offset = 0;

        List<CompletableFuture<Void>> list = new ArrayList<>();

        for (QueueManager<?> queueManager : queueManagerList) {

            Reader reader = new HiveReader(fetchSize, executeSql, hostName, userName,
                    password, queueManager);

            list.add(CompletableFuture.runAsync(reader, readerPool).thenRun(() -> {
                //reader.close();
            }));

            offset += chunkSize;

        }
        return list;
    }

    private List<CompletableFuture<Void>> createWriters(String outputFileName, ExecutorService writerPool, FileType
            fileType, List<QueueManager<?>> queueManagerList) {
        List<CompletableFuture<Void>> list = new ArrayList<>();

        for (int i = 0; i < queueManagerList.size(); i++) {
            Writer writer = null;

            switch (fileType) {
                case CSV -> {
                    writer = new CSVWriter(outputFileName + i, (CSVQueueManager) queueManagerList.get(i));
                }
                case PARQUET -> {
                    writer = new ParquetWriter(outputFileName + i, (ParquetQueueManager) queueManagerList.get(i));
                }
                case DISRUPTOR_CSV -> {
                }
            }

            list.add(CompletableFuture.runAsync(writer, writerPool).thenRun(() -> {
                //reader.close();
            }));
        }
        return list;
    }


    private int getTotalRowsCount(String executeSql) throws SQLException {
        final OracleManager manager = new OracleManager(hostName, userName, password);

        return manager.getTotalRowsCount(executeSql);
    }
}
