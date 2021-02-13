import Queue.FileType;
import Queue.Impl.CSVQueueManager;
import Queue.Impl.ParquetQueueManager;
import Reader.Impl.OracleReader;
import Reader.Reader;
import Writer.Impl.CSVWriter;
import Writer.Impl.ParquetWriter;
import avro.Impl.OracleTransformer;
import picocli.CommandLine;

import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(description = "ADID Transfer", name = "transfer", mixinStandardHelpOptions = true, version = "transfer 1.0")
public class TransferCli implements Callable<Integer> {

    @Option(names = {"-r", "--read-file-type"}, description = "Read File Type (CSV, PARQUET)", defaultValue = "CSV", required = true)
    private FileType readFileType;

    @Option(names = {"-w", "--write-file-type"}, description = "Write File Type (CSV, PARQUET)", defaultValue = "CSV", required = true)
    private FileType writeFileType;

    @Option(names = {"-p", "--parallel"}, description = "read and write in parallel")
    private boolean isParallel;


    public static void main(String[] args) {

        int exitCode = new CommandLine(new TransferCli()).execute(args);
        System.exit(exitCode);

        ParquetQueueManager parquetQueueManager = new ParquetQueueManager(new OracleTransformer(), "jong2", "com.jong2");
        CSVQueueManager csvQueueManager = new CSVQueueManager();
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

    }

    @Override
    public Integer call() throws Exception {
        // TODO:
        return null;
    }
}
