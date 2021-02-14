package avro.Cli;

import Queue.FileType;
import Queue.Impl.CSVQueueManager;
import Queue.Impl.ParquetQueueManager;
import Reader.Impl.OracleReader;
import Reader.Reader;
import Writer.Impl.CSVWriter;
import Writer.Impl.ParquetWriter;
import Writer.Writer;
import avro.Impl.OracleTransformer;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.ParentCommand;

/**
 *                 reader = new OracleReader(fetchSize, "adid_test",
 *                         "jdbc:oracle:thin:@localhost:1521:xe", queue);
 */
@Command(name = "single")
public class SingleCli implements Callable<Integer> {

    @ParentCommand
    private TransferCli transferCli;


    @SuppressWarnings("rawtypes")
    @Override
    public Integer call() throws Exception {

        FileType fileType = transferCli.getFileType();
        int fetchSize = transferCli.getFetchSize();
        String hostName = transferCli.getHostName();
        String tableName = transferCli.getTableName();
        String userName = transferCli.getUserName();
        String password = transferCli.getPassword();

        Reader reader = null;
        Writer writer = null;

        switch (fileType) {
            case PARQUET -> {
                ParquetQueueManager queue = new ParquetQueueManager(new OracleTransformer(), "jong2", "com.jong2");
                reader = new OracleReader(fetchSize, tableName, hostName, userName, password, queue);
                writer = new ParquetWriter("", queue);
            }
            case CSV -> {
                CSVQueueManager queue = new CSVQueueManager();
                reader = new OracleReader(fetchSize, tableName, hostName, userName, password, queue);
                writer = new CSVWriter("", queue);
            }
        }

        Thread readerThread = new Thread(reader);
        Thread writerThread = new Thread(writer);

        long startTime = System.currentTimeMillis();
        readerThread.start();
        writerThread.start();
        try {
            readerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        writerThread.interrupt();
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(estimatedTime / 1000.0);

        return null;
    }
}
