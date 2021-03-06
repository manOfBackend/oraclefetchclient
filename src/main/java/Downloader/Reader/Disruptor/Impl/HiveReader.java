package Downloader.Reader.Disruptor.Impl;

import Downloader.Reader.Disruptor.Reader;
import Queue.Disruptor.ResultSetEventProducer;
import org.apache.hive.jdbc.HiveDriver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class HiveReader extends Reader {

    public HiveReader(int fetchSize, String sql, String hostName, String userName, String password, ResultSetEventProducer producer) {
        super(fetchSize, sql, hostName, userName, password, producer);
    }

    @Override
    public ResultSet createResultSet(Connection conn, String sql, int fetchSize) throws SQLException {
        final Statement statement = conn.createStatement();

        // Hive 옵션 설정
        statement.execute("set hive.server2.thrift.resultset.default.fetch.size=" + fetchSize);
//        statement.execute("set hive.exec.parallel=true");
//        statement.execute("set hive.vectorized.execution.enabled=true");
//        statement.execute("set hive.vectorized.execution.reduce.enabled=true");
        statement.setFetchSize(fetchSize);

        return statement.executeQuery(sql);
    }

    @Override
    public Connection createConnection() throws SQLException {
        final HiveDriver hiveDriver = new HiveDriver();
        final Properties properties = new Properties();
        properties.setProperty("user", userName);
        properties.setProperty("password", password);

        return hiveDriver.connect(hostName, properties);
    }
}
