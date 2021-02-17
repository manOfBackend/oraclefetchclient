package Downloader.Reader.Disruptor.Impl;

import Downloader.Reader.Disruptor.Reader;
import Queue.Disruptor.ResultSetEventProducer;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.driver.OracleDriver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class OracleReader extends Reader {

    public OracleReader(int fetchSize, String tableName, String hostName, String userName, String password, ResultSetEventProducer producer) {
        super(fetchSize, tableName, hostName, userName, password, producer);
    }

    @Override
    public ResultSet createResultSet(Connection conn, String sql, int fetchSize) throws SQLException {

        final OraclePreparedStatement oracleStmt = (OraclePreparedStatement) conn.prepareStatement(sql, OracleResultSet.FETCH_FORWARD);
        oracleStmt.setFetchSize(fetchSize);
        return oracleStmt.executeQuery();

    }

    @Override
    public Connection createConnection(String hostName) throws SQLException {
        final OracleDriver orcDriver = new OracleDriver();
        final Properties properties = new Properties();
        properties.setProperty("user", userName);
        properties.setProperty("password", password);

        return orcDriver.connect(hostName, properties);
    }
}
