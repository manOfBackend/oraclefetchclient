package Downloader.Writer.Disruptor.Impl;

import Downloader.Writer.Disruptor.Writer;
import Queue.Disruptor.RowEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;

public class CSVWriter extends Writer<ResultSet> {
    private com.opencsv.CSVWriter csvWriter;

    public CSVWriter(String outputPath) {
        super(outputPath);
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
    public void onEvent(RowEvent<ResultSet> rowEvent, long sequence, boolean endOfBatch) throws Exception {
        ResultSet resultSet = rowEvent.getResultSet();
//        csvWriter.writeNext();
    }
}
