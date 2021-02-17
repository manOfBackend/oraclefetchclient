package Cli;

import DbManager.Oracle.OracleManager;
import Downloader.Reader.BlockingQueue.Impl.OracleParallelReader;
import Downloader.Reader.BlockingQueue.Impl.OracleReader;
import Downloader.Reader.BlockingQueue.Reader;
import Downloader.Writer.BlockingQueue.Impl.CSVWriter;
import Downloader.Writer.BlockingQueue.Impl.ParquetWriter;
import Downloader.Writer.BlockingQueue.Writer;
import Downloader.Writer.FileType;
import Queue.BlockingQueue.Impl.CSVQueueManager;
import Queue.BlockingQueue.Impl.ParquetQueueManager;
import avro.Impl.OracleTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static picocli.CommandLine.*;

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

    @Option(names = {"-sql", "--execute-sql"}, defaultValue = "select * from adid_test")
    private String executeSql;

    @Option(names = {"-host", "--host-name"}, required = true)
    private String hostName;

    @Option(names = {"-u", "--user-name"}, required = true)
    private String userName;

    @Option(names = {"-p", "--password"}, required = true)
    private String password;


    @Override
    public Integer call() throws Exception {
        //TODO: MULTI THREADING LOGIC

        System.out.println("parallel");
        System.out.println("Threads: " + threadCount);

        String outputFileName = transferCli.getOutputFileName();

        final OracleManager manager = new OracleManager(hostName, userName, password);

        final int totalRowsCount = manager.getTotalRowsCount(executeSql);

        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount * 2);

        int offset = 0;
        final int chunkSize = (int) Math.ceil((double) totalRowsCount / threadCount);
        System.out.println("TotalRows: " + totalRowsCount);
        System.out.println("chunkSize: " + chunkSize);

        List<CompletableFuture<Void>> futureList = new ArrayList<>();

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

            futureList.add(CompletableFuture.runAsync(reader, executorService));
            futureList.add(CompletableFuture.runAsync(writer, executorService));
            offset += chunkSize;
        }

        final CompletableFuture<Void> all = CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new)).thenRunAsync(() -> {
            System.out.println("완료");
        }, executorService);

        System.out.println("시작");
        all.get();

        executorService.shutdown();

        return 0;
    }
}
