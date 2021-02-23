package Downloader.Reader.Disruptor;

import Queue.Disruptor.ResultSetEventProducer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Reader implements Runnable {

    // 쿼리당 가져오는 Row 수
    protected int fetchSize = 10;

    protected final String tableName;

    // DB 접속 URL
    protected final String hostName;

    protected final String userName;

    protected final String password;

    protected final ResultSetEventProducer producer;

    public Reader(int fetchSize, String tableName, String hostName, String userName, String password, ResultSetEventProducer producer) {
        this.fetchSize = fetchSize;
        this.tableName = tableName;
        this.hostName = hostName;
        this.userName = userName;
        this.password = password;
        this.producer = producer;
    }

    public abstract ResultSet createResultSet(Connection conn, String sql, int fetchSize) throws SQLException;

    public abstract Connection createConnection() throws SQLException;

    @Override
    public void run() {
        final String sql = "SELECT * FROM " + tableName;
        System.out.println("Running: " + sql);

        try (Connection conn = createConnection();
             ResultSet resultSet = createResultSet(conn, sql, fetchSize)) {
            if (conn == null) {
                throw new SQLException("no connection");
            }
            if (resultSet == null) {
                throw new SQLException("no resultSet");
            }
            producer.onData(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}