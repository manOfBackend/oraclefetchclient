public class Main {

    public static void main(String[] args) {

        Thread reader = new Thread(new Reader(10000, "adid_test",
                "jdbc:oracle:thin:@localhost:1521:xe"));
        Thread writer = new Thread(CSVWriter.getInstance());


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
