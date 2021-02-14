package avro.Cli;

import Queue.FileType;
import Queue.Impl.ParquetQueueManager;
import Reader.Impl.OracleReader;
import Writer.Impl.ParquetWriter;
import avro.Impl.OracleTransformer;
import picocli.CommandLine;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

/**
 * one to one thread
 */
@Command(description = "ADID Transfer", name = "transfer", mixinStandardHelpOptions = true, version = "transfer 1.0",
        subcommands = {SingleCli.class, ParallelCli.class})
public class TransferCli implements Callable<Integer> {

    @Option(names = {"-r", "--read-file-type"}, description = "Read File Type (CSV, PARQUET)", defaultValue = "CSV", required = true)
    private FileType readFileType;

    @Option(names = {"-w", "--write-file-type"}, description = "Write File Type (CSV, PARQUET)", defaultValue = "CSV", required = true)
    private FileType writeFileType;

    @Option(names = {"-s", "--fetch-size"}, defaultValue = "10000")
    private int fetchSize;

    public static void main(String[] args) {

        int exitCode = new CommandLine(new TransferCli()).execute(args);
        System.exit(exitCode);


    }

    @Override
    public Integer call() throws Exception {
        // TODO:
        ParquetQueueManager parquetQueueManager = new ParquetQueueManager(new OracleTransformer(), "jong2", "com.jong2");

        Thread reader = new Thread(new OracleReader(10000, "adid_test",
                "jdbc:oracle:thin:@localhost:1521:xe", parquetQueueManager));
        Thread writer = new Thread(new ParquetWriter("", parquetQueueManager));

        long startTime = System.currentTimeMillis();
        reader.start();
        //  writer.start();
        try {
            reader.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //   writer.interrupt();
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(estimatedTime / 1000.0);

        return null;
    }
}

