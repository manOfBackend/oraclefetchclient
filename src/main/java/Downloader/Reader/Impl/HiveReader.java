package Downloader.Reader.Impl;

import Downloader.Reader.Reader;
import Queue.BlockingQueue.QueueManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HiveReader extends Reader {


    public HiveReader(int fetchSize, String tableName, String hostName, String userName, String password, QueueManager<?> queueManager) {
        super(fetchSize, tableName, hostName, userName, password, queueManager);
    }

    @Override
    public ResultSet createResultSet(Connection conn, String sql, int fetchSize) throws SQLException {

        return null;
    }

    @Override
    public Connection createConnection(String hostName) throws SQLException {
        return null;
    }
}
