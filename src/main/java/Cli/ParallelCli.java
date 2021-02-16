package Cli;

import Downloader.Writer.FileType;

import java.util.concurrent.Callable;

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
        return 0;
    }
}
