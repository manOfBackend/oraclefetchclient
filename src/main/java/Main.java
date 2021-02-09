import avro.SchemaGenerator;
import avro.SchemaResults;
import avro.SchemaSqlMapping;
import org.apache.avro.reflect.ReflectData;
import org.apache.parquet.example.data.Group;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        Thread reader = new Thread(new Reader(10000, "adid_test",
                "jdbc:oracle:thin:@localhost:1521:xe"));
        Thread writer = new Thread(Writer.getInstance());


        long startTime = System.currentTimeMillis();
        reader.start();
        writer.start();
        try {
            reader.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        writer.interrupt();
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(estimatedTime / 1000.0);




    }
}
