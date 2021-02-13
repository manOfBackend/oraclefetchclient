package Reader.Impl;

import Queue.Impl.CSVQueueManager;
import Queue.Impl.ParquetQueueManager;
import Queue.QueueManager;
import Reader.Reader;
import avro.SchemaGenerator;
import avro.SchemaResults;
import avro.SchemaSqlMapping;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleDriver;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class OracleReader extends Reader {

    public OracleReader(int fetchSize, String tableName, String hostName, QueueManager queueManager) {
        super(fetchSize, tableName, hostName, queueManager);
    }
    private OracleResultSet createResultSet(OracleConnection conn, String sql) throws SQLException {

        final OraclePreparedStatement oracleStmt = (OraclePreparedStatement) conn.prepareStatement(sql, OracleResultSet.FETCH_FORWARD);

        return (OracleResultSet) oracleStmt.executeQuery();

    }

    private OracleConnection createConnection(String hostName) throws SQLException {
        final OracleDriver orcDriver = new OracleDriver();
        final Properties properties = new Properties();
        properties.setProperty("user", "c##jong");
        properties.setProperty("password", "guswhd12");

        final OracleConnection conn = (OracleConnection) orcDriver.connect(hostName, properties);

        return conn;
    }

    @Override
    public void run() {
        final String sql = "SELECT * FROM " + tableName;
        System.out.println("Running: " + sql);

        OracleConnection conn = null;
        OracleResultSet resultSet = null;

        try {
            conn = createConnection(hostName);
            resultSet = createResultSet(conn, sql);

            SchemaResults schemaResults = getSchemaResults(resultSet);
            try {

                Path outputPath = getPath();

                System.out.println(outputPath.toUri().toString());

                queueManager.addAllFetchToQueue(resultSet);
//                ParquetWriter parquetWriter = getParquetWriter(schemaResults, outputPath);
//
//                for (GenericRecord record : records) {
//                    parquetWriter.write(record);
//                }

//                parquetWriter.close();

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            queueManager.addAllFetchToQueue(resultSet);

        } catch (SQLException | IOException throwables) {
            throwables.printStackTrace();
        }
        finally {
            try {
                resultSet.close();
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    private ParquetWriter getParquetWriter(SchemaResults schemaResults, Path outputPath) throws IOException {
        ParquetWriter parquetWriter = AvroParquetWriter.builder(outputPath)
                .withSchema(schemaResults.getParsedSchema())
                //   .withConf(new Configuration())
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .build();
        return parquetWriter;
    }

    private Path getPath() throws IOException {
        java.nio.file.Path tempFile = Paths.get("c:\\aaa.parquet");

        Path outputPath = new Path(tempFile.toUri());

        final LocalFileSystem localFileSystem = FileSystem.getLocal(new Configuration());

        File file = localFileSystem.pathToFile(outputPath);
        if (file.exists()) {
            file.delete();
        }
        return outputPath;
    }

    private SchemaResults getSchemaResults(OracleResultSet resultSet) throws SQLException {
        SchemaGenerator generator = new SchemaGenerator();

        String schemaName = "jong2";
        String namespace = "com.jong2";

        SchemaResults schemaResults = generator.generateSchema(resultSet, schemaName, namespace);
        return schemaResults;
    }

}

