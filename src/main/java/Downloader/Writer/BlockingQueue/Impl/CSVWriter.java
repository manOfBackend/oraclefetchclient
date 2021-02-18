package Downloader.Writer.BlockingQueue.Impl;

import Downloader.Writer.BlockingQueue.Writer;
import Queue.BlockingQueue.Impl.CSVQueueManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class CSVWriter extends Writer {

    public CSVWriter(String outputPath, CSVQueueManager queueManager) {
        super(outputPath, queueManager);
        Path path = Paths.get(outputPath);
    }

    @Override
    public void run() {

        System.out.println("Writer : " + outputPath);
        CSVQueueManager queue = (CSVQueueManager) queueManager;
        Path path = Paths.get(outputPath);

        try (final com.opencsv.CSVWriter csvWriter = new com.opencsv.CSVWriter(Files.newBufferedWriter(path,
                StandardCharsets.UTF_8), com.opencsv.CSVWriter.DEFAULT_SEPARATOR,
                com.opencsv.CSVWriter.NO_QUOTE_CHARACTER, com.opencsv.CSVWriter.NO_ESCAPE_CHARACTER,
                com.opencsv.CSVWriter.DEFAULT_LINE_END))
        {
            while (true) {

                // TODO: thread interrupt check -> thread shutdownnow 인터럽트 처리를 여기서 할 것 (CATCH 추가)

                Optional<List<String[]>> optionalList = queue.getList();

                // TODO: empty라고 break는 안 된다. 10초 대기한다고 해도
                if (optionalList.isEmpty()) {
                    break;
                }

                List<String[]> list = optionalList.get();

                for (String[] line : list) {
                    csvWriter.writeNext(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
