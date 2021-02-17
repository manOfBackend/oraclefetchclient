package Downloader.Reader.BlockingQueue;

import Queue.BlockingQueue.QueueManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Reader implements Runnable {

    // 쿼리당 가져오는 Row 수
    protected int fetchSize = 10;

    protected final String sql;

    // DB 접속 URL
    protected final String hostName;

    protected final String userName;

    protected final String password;

    protected final QueueManager<?> queueManager;


    public Reader(int fetchSize, String sql, String hostName, String userName, String password, QueueManager<?> queueManager) {
        this.fetchSize = fetchSize;
        this.sql = sql;
        this.hostName = hostName;
        this.userName = userName;
        this.password = password;
        this.queueManager = queueManager;
    }
    public String getSql() {
        return sql;
    }


    public abstract ResultSet createResultSet(Connection conn, String sql, int fetchSize) throws SQLException;

    public abstract Connection createConnection(String hostName) throws SQLException;

    @Override
    public void run() {
        System.out.println("Running: " + sql);

        try (Connection conn = createConnection(hostName);
             ResultSet resultSet = createResultSet(conn, sql, fetchSize)) {
            if (conn == null) {
                throw new SQLException("no connection");
            }
            if (resultSet == null) {
                throw new SQLException("no resultSet");
            }
            queueManager.addAllFetchToQueue(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
