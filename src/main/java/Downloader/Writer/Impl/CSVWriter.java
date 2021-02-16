package Downloader.Writer.Impl;

import Downloader.Writer.Writer;
import Queue.BlockingQueue.Impl.CSVQueueManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class CSVWriter extends Writer {

    private com.opencsv.CSVWriter csvWriter;

    public CSVWriter(String outputPath, CSVQueueManager queueManager) {
        super(outputPath, queueManager);
        Path path = Paths.get(outputPath);

        try {
            csvWriter = new com.opencsv.CSVWriter(Files.newBufferedWriter(path,
                    StandardCharsets.UTF_8), com.opencsv.CSVWriter.DEFAULT_SEPARATOR,
                    com.opencsv.CSVWriter.NO_QUOTE_CHARACTER, com.opencsv.CSVWriter.NO_ESCAPE_CHARACTER,
                    com.opencsv.CSVWriter.DEFAULT_LINE_END);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void flushAndClose() {
        try {
            if (csvWriter == null) return;
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        CSVQueueManager queue = (CSVQueueManager) queueManager;
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

        flushAndClose();

    }
}
