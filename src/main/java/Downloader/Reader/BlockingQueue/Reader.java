package Downloader.Reader.BlockingQueue;

import Queue.BlockingQueue.QueueManager;
import oracle.ons.Closable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 오라클 싱글, 오라클 병렬, 하이브 등 DBMS에서 Fetch를 받아오는 역할
 * BlockingQueue를 사용하는 버전
 * run() 메서드를 템플릿 메서드로 구현
 */
public abstract class Reader implements Runnable, Closable {

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

    public abstract Connection createConnection() throws SQLException;

    @Override
    public void run() {
        
        try (Connection conn = createConnection();
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
