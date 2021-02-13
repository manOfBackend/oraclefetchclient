package Writer.Impl;

import Writer.Writer;
import avro.SchemaGenerator;
import avro.SchemaResults;
import oracle.jdbc.OracleResultSet;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import java.sql.SQLException;

public class ParquetWriter extends Writer {

    @Override
    public void run() {

        org.apache.parquet.hadoop.ParquetWriter parquetWriter = AvroParquetWriter.builder(outputPath)
                .withSchema(schemaResults.getParsedSchema())
                //   .withConf(new Configuration())
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .build();
        return parquetWriter;
    }
}
