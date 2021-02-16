package Downloader.Writer.BlockingQueue.Impl;

import Downloader.Writer.BlockingQueue.Writer;
import Queue.BlockingQueue.Impl.ParquetQueueManager;
import avro.Schema.SchemaResults;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.column.ParquetProperties;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("rawtypes")
public class ParquetWriter extends Writer {

    private org.apache.parquet.hadoop.ParquetWriter<GenericRecord> parquetWriter;

    public ParquetWriter(String outputPath, ParquetQueueManager queueManager) {
        super(outputPath, queueManager);


    }

    private void initWriter() {

        if (this.parquetWriter == null) {
            synchronized (this) {
                if (this.parquetWriter == null) {
                    System.out.println("init");
                    Path path = getOutputFilePath();

                    SchemaResults schemaResults = ((ParquetQueueManager) getQueueManager()).getSchemaResults();

                    try {
                        this.parquetWriter = AvroParquetWriter.<GenericRecord>builder(path)
                                .withSchema(schemaResults.getParsedSchema())
                                .withCompressionCodec(CompressionCodecName.GZIP)
                                .withRowGroupSize(16 * 1024 * 1024)
                                .withPageSize(4 * 1024 * 1024)
                                .withWriterVersion(ParquetProperties.WriterVersion.PARQUET_2_0)
                                .build();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private Path getOutputFilePath() {
        java.nio.file.Path tempFile = Paths.get(getOutputPath());

        Path outputPath = new Path(tempFile.toUri());

        try {
            final LocalFileSystem localFileSystem = FileSystem.getLocal(new Configuration());
            File file = localFileSystem.pathToFile(outputPath);
            if (file.exists()) {
                file.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return outputPath;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {

        int count = 0;
        ParquetQueueManager queue = (ParquetQueueManager) queueManager;
        while (true) {
            try {
                Optional<List<GenericRecord>> optionalList = queue.getList();
                if (optionalList.isEmpty()) {
                    System.out.println("empty");
                    break;
                }
                initWriter();

                List<GenericRecord> list = optionalList.get();

                System.out.println(++count + "," + parquetWriter.getDataSize());
                for (GenericRecord record : list) {
                    this.parquetWriter.write(record);

                }

            } catch(IOException ioException) {
                ioException.printStackTrace();
                break;
            }
        }

        try {
            parquetWriter.close();
            System.out.println("parquet close");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
