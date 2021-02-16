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

        CSVQueueManager queue = (CSVQueueManager) queueManager;
        Path path = Paths.get(outputPath);

        try (final com.opencsv.CSVWriter csvWriter = new com.opencsv.CSVWriter(Files.newBufferedWriter(path,
                StandardCharsets.UTF_8), com.opencsv.CSVWriter.DEFAULT_SEPARATOR,
                com.opencsv.CSVWriter.NO_QUOTE_CHARACTER, com.opencsv.CSVWriter.NO_ESCAPE_CHARACTER,
                com.opencsv.CSVWriter.DEFAULT_LINE_END))
        {
            while (true) {

                Optional<List<String[]>> optionalList = queue.getList();

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
