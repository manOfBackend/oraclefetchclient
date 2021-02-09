import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Writer implements Runnable{

    private static com.opencsv.CSVWriter csvWriter;

    private static Writer writer;

    private static String outputPath = "output.csv";

    public static String getOutputPath() {
        return outputPath;
    }

    public static void setOutputPath(String outputPath) {
        Writer.outputPath = outputPath;
    }

    private Writer(String path) {

        initWriter(path);
    }

    private void initWriter(String path) {
        Path myPath = Paths.get(path);

        try {
            this.csvWriter = new com.opencsv.CSVWriter(Files.newBufferedWriter(myPath,
                    StandardCharsets.UTF_8), com.opencsv.CSVWriter.DEFAULT_SEPARATOR,
                    com.opencsv.CSVWriter.NO_QUOTE_CHARACTER, com.opencsv.CSVWriter.NO_ESCAPE_CHARACTER,
                    com.opencsv.CSVWriter.DEFAULT_LINE_END);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Writer getInstance() {
        if (writer == null) {
            synchronized (Writer.class) {
                if (writer == null) {
                    writer = new Writer(getOutputPath());
                }
            }
        }
        return writer;
    }

    private void flushAndClose(){
        try {
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {

        while(true) {

            try {
                Optional<List<String[]>> optionalList = QueueManager.getList();
                if (optionalList.isEmpty()) {
                    break;
                }

                List<String[]> list = optionalList.get();
                for (String[] line : list) {
                    csvWriter.writeNext(line);
                }
            } catch (InterruptedException e) {
                break;
            }


        }

        flushAndClose();


    }
}
