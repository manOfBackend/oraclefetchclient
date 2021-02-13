import Queue.Impl.ParquetQueueManager;
import Reader.Impl.OracleReader;
import Reader.Reader;
import Writer.Impl.CSVWriter;

public class Main {

    public static void main(String[] args) {


        ParquetQueueManager parquetQueueManager = new ParquetQueueManager();
        Thread reader = new Thread(new OracleReader(10000, "adid_test",
                "jdbc:oracle:thin:@localhost:1521:xe", parquetQueueManager));
        Thread writer = new Thread(new CSVWriter(""));

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
}
