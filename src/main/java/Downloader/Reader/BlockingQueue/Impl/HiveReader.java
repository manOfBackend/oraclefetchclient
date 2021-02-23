package Downloader.Reader.BlockingQueue.Impl;

import Downloader.Reader.BlockingQueue.Reader;
import Queue.BlockingQueue.QueueManager;
import Queue.Disruptor.ResultSetEventProducer;
import org.apache.hive.jdbc.HiveDriver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class HiveReader extends Reader {

    private final static HiveDriver hiveDriver = new HiveDriver();

    public HiveReader(int fetchSize, String sql, String hostName, String userName, String password, QueueManager<?> queueManager) {
        super(fetchSize, sql, hostName, userName, password, queueManager);
    }

    @Override
    public ResultSet createResultSet(Connection conn, String sql, int fetchSize) throws SQLException {
        final Statement statement = conn.createStatement();
        statement.execute("set hive.server2.thrift.resultset.default.fetch.size=" + fetchSize);
//        statement.execute("set hive.exec.parallel=true");
//        statement.execute("set hive.vectorized.execution.enabled=true");
//        statement.execute("set hive.vectorized.execution.reduce.enabled=true");
        statement.setFetchSize(fetchSize);

        return statement.executeQuery(sql);
    }

    @Override
    public Connection createConnection() throws SQLException {

        final Properties properties = new Properties();
        properties.setProperty("user", userName);
        properties.setProperty("password", password);

        return hiveDriver.connect(hostName, properties);
    }

    @Override
    public void close() {

    }
}
