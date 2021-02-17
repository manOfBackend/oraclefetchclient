package Cli;

import DbManager.Oracle.OracleManager;
import Downloader.Reader.BlockingQueue.Impl.OracleParallelReader;
import Downloader.Reader.BlockingQueue.Reader;
import Downloader.Writer.BlockingQueue.Impl.CSVWriter;
import Downloader.Writer.BlockingQueue.Impl.ParquetWriter;
import Downloader.Writer.BlockingQueue.Writer;
import Downloader.Writer.FileType;
import Queue.BlockingQueue.Impl.CSVQueueManager;
import Queue.BlockingQueue.Impl.ParquetQueueManager;
import avro.Impl.OracleTransformer;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static picocli.CommandLine.*;

/**
 * 멀티 스레딩 fetch 커맨드 클래스
 * 현재 오라클만 지원함
 */
@Command(name = "parallel")
public class ParallelCli implements Callable<Integer> {

    @ParentCommand
    private TransferCli transferCli;

    @Option(names = {"-c", "--thread-count"}, defaultValue = "4")
    private int threadCount;

    @Option(names = {"-type", "--file-type"}, description = "Write File Type (CSV, PARQUET)", required = true)
    private FileType fileType;

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
        //TODO: MULTI THREADING LOGIC
        // 싱글과 합쳐도 좋을 듯
        // 좀 더 안전한지 테스트가 필요함

        String executeSql = Files.readString(executeSqlFile.toPath());
        String outputFileName = transferCli.getOutputFileName();

        final OracleManager manager = new OracleManager(hostName, userName, password);

        final int totalRowsCount = manager.getTotalRowsCount(executeSql);

        final ExecutorService readerPool = Executors.newFixedThreadPool(threadCount);
        final ExecutorService writerPool = Executors.newFixedThreadPool(threadCount);

        int offset = 0;
        final int chunkSize = (int) Math.ceil((double) totalRowsCount / threadCount);

        List<CompletableFuture<Void>> readerList = new ArrayList<>();
        List<CompletableFuture<Void>> writerList = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {

            System.out.println(i + " : " + executeSql);
            final OracleManager oracleManager = new OracleManager(hostName, userName, password);

            Reader reader = null;
            Writer writer = null;

            switch (fileType) {
                case PARQUET -> {
                    ParquetQueueManager queue = new ParquetQueueManager(new OracleTransformer(), "jong2", "com.jong2");
                    reader = new OracleParallelReader(executeSql, fetchSize, offset, chunkSize, oracleManager, queue);
                    writer = new ParquetWriter(outputFileName + i, queue);
                }
                case CSV -> {
                    CSVQueueManager queue = new CSVQueueManager();
                    reader = new OracleParallelReader(executeSql, fetchSize, offset, chunkSize, oracleManager, queue);
                    writer = new CSVWriter(outputFileName + i, queue);
                }
                default -> {
                    return -1;
                }
            }

            readerList.add(CompletableFuture.runAsync(reader, readerPool));
            writerList.add(CompletableFuture.runAsync(writer, writerPool));
            offset += chunkSize;
        }

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
}
