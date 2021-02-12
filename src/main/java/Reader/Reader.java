package Reader;

import Queue.QueueManager;
import avro.ResultSetTransformer;
import avro.SchemaGenerator;
import avro.SchemaResults;
import avro.SchemaSqlMapping;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleDriver;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Reader implements Runnable {

    // 쿼리당 가져오는 Row 수
    private int fetchSize = 10;

    private final String tableName;

    // DB 접속 URL
    private final String hostName;

    public Reader(int fetchSize, String tableName, String hostName) {
        this.fetchSize = fetchSize;
        this.tableName = tableName;
        this.hostName = hostName;
    }

    /**
     * ResultSet이 가져온 쿼리 결과를 CSV에 저장하는 함수(기본 저장 경로: output.csv)
     *
     * @param rs
     * @throws SQLException
     */
    private void addAllFetchToQueue(OracleResultSet rs) throws SQLException {

        List<GenericData.Record> list = new ArrayList<>();
        while (rs.next()) {
//            final int n = rs.getMetaData().getColumnCount();
//            final String[] line = new String[n];
//            for (int i = 1; i<=n; i++) {
//                line[i-1] = rs.getString(i);
//            }
            GenericRecordBuilder builder = new GenericRecordBuilder(schemaResults.getParsedSchema());

            list.add(line);
            if (list.size() == fetchSize) {
                QueueManager.addList(new ArrayList<>(list));
                list.clear();
            }
        }
        if (list.size() > 0) {
            QueueManager.addList(new ArrayList<>(list));
        }
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

            SchemaGenerator generator = new SchemaGenerator();

            String schemaName = "jong2";
            String namespace = "com.jong2";

            try {
                SchemaResults schemaResults = generator.generateSchema(resultSet, schemaName, namespace);

                java.nio.file.Path tempFile = Paths.get("c:\\aaa.parquet");

                org.apache.hadoop.fs.Path outputPath = new org.apache.hadoop.fs.Path(tempFile.toUri());

                final LocalFileSystem localFileSystem = FileSystem.getLocal(new Configuration());

                File file = localFileSystem.pathToFile(outputPath);
                if (file.exists()) {
                    file.delete();
                }

                System.out.println(outputPath.toUri().toString());

                ParquetWriter parquetWriter = AvroParquetWriter.builder(outputPath)
                        .withSchema(schemaResults.getParsedSchema())
                     //   .withConf(new Configuration())
                        .withCompressionCodec(CompressionCodecName.SNAPPY)
                        .build();


                List<GenericRecord> records = new ArrayList<>();

                int limit = 0;
                while (resultSet.next()) {

                    limit++;
                    if (limit >= 10000) break;
                    GenericRecordBuilder builder = new GenericRecordBuilder(schemaResults.getParsedSchema());

                    for (SchemaSqlMapping mapping : schemaResults.getMappings()) {

                        builder.set(
                                schemaResults.getParsedSchema().getField(mapping.getSchemaName()),
                                ResultSetTransformer.extractResult(mapping, resultSet));
                    }

                    GenericRecord record = builder.build();
                    records.add(record);

                }

                System.out.println(records.size());

                for (GenericRecord record : records) {
                    parquetWriter.write(record);
                }

                parquetWriter.close();

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            addAllFetchToQueue(resultSet);
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

}
