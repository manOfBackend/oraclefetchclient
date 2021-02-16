package Cli;

import Queue.BlockingQueue.FileType;
import picocli.CommandLine;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

/**
 * main cli
 */
@Command(description = "ADID Transfer", name = "transfer", mixinStandardHelpOptions = true, version = "transfer 1.0",
        subcommands =
                {
                        SingleCli.class,
                        ParallelCli.class,
                        uploadCli.class
                })
public class TransferCli implements Callable<Integer> {

    @Option(names = {"-type", "--file-type"}, description = "Write File Type (CSV, PARQUET)", required = true)
    private FileType fileType;

    @Option(names = {"-o", "--output"}, defaultValue = "output_adid")
    private String outputFileName;

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

    public static void main(String[] args) {

        int exitCode = new CommandLine(new TransferCli()).execute(args);
        System.exit(exitCode);


    }

    public String getHostName() {
        return hostName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public FileType getFileType() {
        return fileType;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("main cli");
        return null;
    }
}

