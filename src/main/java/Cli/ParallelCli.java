package Cli;

import DbManager.Oracle.OracleManager;
import DbManager.Oracle.OracleParallelWorker;
import Downloader.Writer.FileType;

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

    @Option(names = {"-sql", "--execute-sql"}, required = true)
    private String executeSql;

    @Option(names = {"-t", "--table-name"}, required = true)
    private String tableName;

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

        OracleManager manager = new OracleManager(hostName, userName, password);

        final int totalRowsCount = manager.getTotalRowsCount(tableName);

        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        int offset = 0;
        final int chunkSize = (int) Math.ceil((double) totalRowsCount / threadCount);

        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futureList.add(CompletableFuture.runAsync(
                    new OracleParallelWorker(executeSql, offset, chunkSize,
                            new OracleManager(hostName, userName, password)), executorService)
            );
            offset += chunkSize;
        }

        final CompletableFuture<Void> allFutures = CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new))
                .thenRun(() -> { System.out.println("finish"); });

        allFutures.get();

        return 0;
    }
}
