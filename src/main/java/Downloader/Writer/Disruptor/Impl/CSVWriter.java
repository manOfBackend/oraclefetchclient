package Downloader.Writer.Disruptor.Impl;

import Downloader.Writer.Disruptor.Writer;
import Queue.Disruptor.ResultSetEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;

public class CSVWriter extends Writer {
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
    public void onEvent(ResultSetEvent resultSetEvent, long sequence, boolean endOfBatch) throws Exception {
        ResultSet resultSet = resultSetEvent.getResultSet();

        // RingBuffer에서 ResultSet를 꺼내 처리
        while (resultSet.next()) {
            /** Logic **/
//          csvWriter.writeNext()
        };
    }
}
