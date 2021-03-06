//package Cli;
//
//import Downloader.Reader.BlockingQueue.Impl.OracleReader;
//import Downloader.Reader.BlockingQueue.Reader;
//import Downloader.Reader.Disruptor.Impl.HiveReader;
//import Downloader.Reader.ReaderType;
//import Downloader.Writer.BlockingQueue.Impl.CSVWriter;
//import Downloader.Writer.BlockingQueue.Impl.ParquetWriter;
//import Downloader.Writer.BlockingQueue.Writer;
//import Downloader.Writer.FileType;
//import Queue.BlockingQueue.Impl.CSVQueueManager;
//import Queue.BlockingQueue.Impl.ParquetQueueManager;
//import Queue.Disruptor.DisruptorConfiguration;
//import Queue.Disruptor.DisruptorProperties;
//import Queue.Disruptor.ResultSetEvent;
//import Queue.Disruptor.ResultSetEventProducer;
//import avro.Impl.OracleTransformer;
//import com.lmax.disruptor.BlockingWaitStrategy;
//import com.lmax.disruptor.RingBuffer;
//import com.lmax.disruptor.WaitStrategy;
//import com.lmax.disruptor.dsl.ProducerType;
//import com.lmax.disruptor.util.DaemonThreadFactory;
//
//import java.io.File;
//import java.nio.file.Files;
//import java.util.concurrent.Callable;
//
//import static picocli.CommandLine.*;
//
///**
// * DB FETCH 스레드 1개와 FILE WRITE 스레드 1개로 작업하는 커맨드 클래스
// */
//@Command(name = "single", description = "one to one thread")
//public class SingleCli implements Callable<Integer> {
//
//    @ParentCommand
//    private TransferCli transferCli;
//
//    @Option(names = {"-type", "--file-type"}, description = "File Write Type (CSV, PARQUET)", required = true)
//    private FileType fileType;
//
//    @Option(names = {"-reader", "--reader-type"}, description = "Fetch Reader Type (HIVE, ORACLE)", required = true)
//    private ReaderType readerType;
//
//    @Option(names = {"-sql", "--execute-sql"}, required = true)
//    private File executeSqlFile;
//
//    @Option(names = {"-s", "--fetch-size"}, defaultValue = "10000")
//    private int fetchSize;
//
//    @Option(names = {"--ring-buffer-size"}, defaultValue = "1024")
//    private int ringBufferSize;
//
//    @Option(names = {"-host", "--host-name"}, required = true)
//    private String hostName;
//
//    @Option(names = {"-u", "--user-name"}, required = true)
//    private String userName;
//
//    @Option(names = {"-p", "--password"}, required = true)
//    private String password;
//
//    @Override
//    public Integer call() throws Exception {
//
//        String outputFileName = transferCli.getOutputFileName();
//        String executeSql = Files.readString(executeSqlFile.toPath());
//
//        System.out.println("fetchSize = " + fetchSize);
//        System.out.println("outputFileName = " + outputFileName);
//
//        Reader reader = null;
//        Writer writer = null;
//
//        switch (fileType) {
//            case PARQUET -> {
//                ParquetQueueManager queue = new ParquetQueueManager(new OracleTransformer(), "jong2", "com.jong2");
//                reader = new OracleReader(fetchSize, executeSql, hostName, userName, password, queue);
//                writer = new ParquetWriter(outputFileName, queue);
//            }
//            case CSV -> {
//                CSVQueueManager queue = new CSVQueueManager();
//                reader = new OracleReader(fetchSize, executeSql, hostName, userName, password, queue);
//                writer = new CSVWriter(outputFileName, queue);
//            }
//
//            case DISRUPTOR_CSV -> {
//                /*********** 옵션으로 받아 처리하도록 수정 ************/
//                WaitStrategy waitStrategy = new BlockingWaitStrategy();
//
//                DisruptorProperties properties = new DisruptorProperties(ringBufferSize, waitStrategy, ProducerType.SINGLE, DaemonThreadFactory.INSTANCE);
//                /************************************************/
//
//
//                // property를 토대로 disruptor 생성
//                DisruptorConfiguration disruptor = new DisruptorConfiguration(properties);
//
//                // Writer, Reader Class명 변경해야함
//                Downloader.Writer.Disruptor.Writer writer1 = new Downloader.Writer.Disruptor.Impl.CSVWriter(outputFileName);
//
//                RingBuffer<ResultSetEvent> ringBuffer = disruptor.run(writer1);
//                ResultSetEventProducer resultSetEventProducer = new ResultSetEventProducer(ringBuffer);
//
//                Downloader.Reader.Disruptor.Reader reader1 = new HiveReader(fetchSize, executeSql, hostName, userName, password, resultSetEventProducer);
//
//                /** 밑에서 readerThread만 start()하면 Write은 Disruptor에서 콜백함수로 알아서 작동됨 **/
//            }
//            default -> {
//                return -1;
//            }
//        }
//
//        Thread readerThread = new Thread(reader);
//        Thread writerThread = new Thread(writer);
//
//        long startTime = System.currentTimeMillis();
//        readerThread.start();
//        writerThread.start();
//        try {
//            readerThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("reader 종료");
//        //writerThread.join();
//        writerThread.interrupt();
//        writerThread.join();
//        long estimatedTime = System.currentTimeMillis() - startTime;
//        System.out.println(estimatedTime / 1000.0);
//
//        return 0;
//    }
//}
