package Downloader.Reader.Disruptor.Impl;

import DbManager.Oracle.OracleManager;
import Downloader.Reader.Disruptor.Reader;
import Queue.BlockingQueue.QueueManager;
import Queue.Disruptor.ResultSetEventProducer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OracleParallelReader extends Reader {
    private final int offset;

    private final int limit;

    private static final String rowsFetchSqlFormat = "%s offset %d rows fetch next %d rows only";

    private final OracleManager oracleManager;

    public OracleParallelReader(String executeSql, int fetchSize, int offset, int limit, OracleManager oracleManager, ResultSetEventProducer producer) {
        super(fetchSize, executeSql, oracleManager.getHostName(), oracleManager.getUserName(), oracleManager.getPassword(), producer);

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
}
