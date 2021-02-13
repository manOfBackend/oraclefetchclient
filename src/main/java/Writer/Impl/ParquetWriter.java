package Writer.Impl;

import Queue.Impl.CSVQueueManager;
import Queue.Impl.ParquetQueueManager;
import Queue.QueueManager;
import Writer.Writer;
import avro.SchemaResults;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("rawtypes")
public class ParquetWriter extends Writer {

    private org.apache.parquet.hadoop.ParquetWriter parquetWriter;

    public ParquetWriter(String outputPath, ParquetQueueManager queueManager) {
        super(outputPath, queueManager);

        Path path = getOutputFilePath();

        SchemaResults schemaResults = queueManager.getSchemaResults();

        org.apache.parquet.hadoop.ParquetWriter parquetWriter = null;
        try {
            this.parquetWriter = AvroParquetWriter.builder(path)
                    .withSchema(schemaResults.getParsedSchema())
                    //   .withConf(new Configuration())
                    .withCompressionCodec(CompressionCodecName.SNAPPY)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
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

    @Override
    public void run() {
        while (true) {

            try {
                ParquetQueueManager queue = (ParquetQueueManager) queueManager;

                Optional<List<GenericRecord>> optionalList = queue.getList();
                if (optionalList.isEmpty()) {
                    break;
                }

                List<GenericRecord> list = optionalList.get();

                for (GenericRecord record : list) {
                    parquetWriter.write(record);
                }

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
                break;
            }
            finally {
                try {
                    parquetWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
