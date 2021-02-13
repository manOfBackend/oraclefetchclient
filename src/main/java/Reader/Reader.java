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

public abstract class Reader implements Runnable {

    // 쿼리당 가져오는 Row 수
    protected int fetchSize = 10;

    protected final String tableName;

    // DB 접속 URL
    protected final String hostName;

    protected final QueueManager<?> queueManager;


    public Reader(int fetchSize, String tableName, String hostName, QueueManager<?> queueManager) {
        this.fetchSize = fetchSize;
        this.tableName = tableName;
        this.hostName = hostName;
        this.queueManager = queueManager;
    }

    /**
     * ResultSet이 가져온 쿼리 결과를 CSV에 저장하는 함수(기본 저장 경로: output.csv)
     *
     * @param rs
     * @throws SQLException
     */
    protected void addAllFetchToQueue(OracleResultSet rs) throws SQLException {


    }


}
