package Downloader.Reader.BlockingQueue;

import Queue.BlockingQueue.QueueManager;

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

    protected final QueueManager<?> queueManager;


    public Reader(int fetchSize, String tableName, String hostName, String userName, String password, QueueManager<?> queueManager) {
        this.fetchSize = fetchSize;
        this.tableName = tableName;
        this.hostName = hostName;
        this.userName = userName;
        this.password = password;
        this.queueManager = queueManager;
    }

    public abstract ResultSet createResultSet(Connection conn, String sql, int fetchSize) throws SQLException;

    public abstract Connection createConnection(String hostName) throws SQLException;

    @Override
    public void run() {
        final String sql = "SELECT * FROM " + tableName;
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
