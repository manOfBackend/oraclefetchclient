package Cli;

import Downloader.Reader.Impl.HiveReader;
import Queue.BlockingQueue.FileType;
import Queue.BlockingQueue.Impl.CSVQueueManager;
import Queue.BlockingQueue.Impl.ParquetQueueManager;
import Downloader.Reader.Impl.OracleReader;
import Downloader.Reader.Reader;
import Downloader.Writer.BlockingQueue.Impl.CSVWriter;
import Downloader.Writer.BlockingQueue.Impl.ParquetWriter;
import Downloader.Writer.BlockingQueue.Writer;
import Queue.Disruptor.DisruptorConfiguration;
import Queue.Disruptor.DisruptorProperties;
import Queue.Disruptor.Impl.CSVRowEventProducer;
import Queue.Disruptor.RowEvent;
import avro.Impl.OracleTransformer;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;

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

    @Override
    public Integer call() throws Exception {

        FileType fileType = transferCli.getFileType();
        int fetchSize = transferCli.getFetchSize();
        String hostName = transferCli.getHostName();
        String tableName = transferCli.getTableName();
        String userName = transferCli.getUserName();
        String password = transferCli.getPassword();
        String outputFileName = transferCli.getOutputFileName();

        System.out.println("fetchSize = " + fetchSize);
        System.out.println("outputFileName = " + outputFileName);

        Reader reader = null;
        Writer writer = null;

        switch (fileType) {
            case PARQUET -> {
                ParquetQueueManager queue = new ParquetQueueManager(new OracleTransformer(), "jong2", "com.jong2");
                reader = new OracleReader(fetchSize, tableName, hostName, userName, password, queue);
                writer = new ParquetWriter(outputFileName, queue);
            }
            case CSV -> {
                CSVQueueManager queue = new CSVQueueManager();
                reader = new OracleReader(fetchSize, tableName, hostName, userName, password, queue);
                writer = new CSVWriter(outputFileName, queue);
            }

            case DISRUPTOR_CSV -> {
                int ringBufferSize = 1024;
                WaitStrategy waitStrategy = new SleepingWaitStrategy();

                DisruptorProperties properties = new DisruptorProperties(ringBufferSize, waitStrategy, ProducerType.SINGLE, DaemonThreadFactory.INSTANCE);
                DisruptorConfiguration disruptor = new DisruptorConfiguration(properties);

                RowEvent<String[]> rowEvent;
                reader = new HiveReader(fetchSize, tableName, hostName, userName, password);
                Downloader.Writer.Disruptor.Writer writer1 = new Downloader.Writer.Disruptor.Impl.CSVWriter(outputFileName);

                RingBuffer<RowEvent<?>> ringBuffer = disruptor.run(writer1);
                CSVRowEventProducer csvRowEventProducer = new CSVRowEventProducer(ringBuffer);

            }
            default -> {
                return -1;
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
        System.out.println("reader 종료");
        //writerThread.join();
        writerThread.interrupt();
        writerThread.join();
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(estimatedTime / 1000.0);

        return 0;
    }
}
