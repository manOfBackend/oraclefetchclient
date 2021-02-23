package Downloader.Reader.BlockingQueue.Impl;

import DbManager.Oracle.OracleManager;
import Downloader.Reader.BlockingQueue.Reader;
import Queue.BlockingQueue.QueueManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 오라클 Fetch 병렬 처리 담당
 */
public class OracleParallelReader extends Reader {

    private final int offset;

    private final int limit;

    private static final String rowsFetchSqlFormat = "%s offset %d rows fetch next %d rows only";

    private final OracleManager oracleManager;

    public OracleParallelReader(String executeSql, int fetchSize, int offset, int limit, OracleManager oracleManager, QueueManager<?> queueManager) {
        super(fetchSize, executeSql, oracleManager.getHostName(), oracleManager.getUserName(), oracleManager.getPassword(), queueManager);

        this.offset = offset;
        this.limit = limit;
        this.oracleManager = oracleManager;

    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    @Override
    public ResultSet createResultSet(Connection conn, String sql, int fetchSize) throws SQLException {
        String formattedSql = String.format(rowsFetchSqlFormat, sql, offset, limit);
        System.out.println("formattedSql: " + formattedSql);
        return oracleManager.getResultSet(formattedSql, fetchSize);
    }

    @Override
    public Connection createConnection() throws SQLException {
        return oracleManager.getConnection();
    }

    @Override
    public void close() {
        try {
            oracleManager.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
