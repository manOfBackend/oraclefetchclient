package Downloader.Reader.BlockingQueue.Impl;

import DbManager.Oracle.OracleManager;
import Downloader.Reader.BlockingQueue.Reader;
import Queue.BlockingQueue.QueueManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OracleParallelReader extends Reader {

    //             cur.execute(self.execute_query + ' offset ' + str(offset) +
    //                        ' rows fetch next ' + str(total_chunk_count) + ' rows only')
    private int offset;

    private int limit;

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

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public OracleManager getOracleManager() {
        return oracleManager;
    }

    @Override
    public ResultSet createResultSet(Connection conn, String sql, int fetchSize) throws SQLException {
        String formattedSql = String.format(rowsFetchSqlFormat, sql, offset, limit);
        System.out.println("formattedSql: " + formattedSql);
        return oracleManager.getResultSet(formattedSql, fetchSize);
    }

    @Override
    public Connection createConnection(String hostName) throws SQLException {
        return oracleManager.createConnection(hostName, userName, password);
    }

}
