package Reader.Impl;

import Queue.QueueManager;
import Reader.Reader;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleDriver;

import java.sql.SQLException;
import java.util.Properties;

public class OracleReader extends Reader {

    @SuppressWarnings("rawtypes")
    public OracleReader(int fetchSize, String tableName, String hostName, String userName, String password, QueueManager queueManager) {
        super(fetchSize, tableName, hostName, userName, password, queueManager);
    }

    private OracleResultSet createResultSet(OracleConnection conn, String sql) throws SQLException {

        final OraclePreparedStatement oracleStmt = (OraclePreparedStatement) conn.prepareStatement(sql, OracleResultSet.FETCH_FORWARD);

        return (OracleResultSet) oracleStmt.executeQuery();

    }

    private OracleConnection createConnection(String hostName) throws SQLException {
        final OracleDriver orcDriver = new OracleDriver();
        final Properties properties = new Properties();
        properties.setProperty("user", userName);
        properties.setProperty("password", password);

        return (OracleConnection) orcDriver.connect(hostName, properties);
    }

    @Override
    public void run() {
        final String sql = "SELECT * FROM " + tableName;
        System.out.println("Running: " + sql);

        OracleConnection conn = null;
        OracleResultSet resultSet = null;

        try {
            conn = createConnection(hostName);

            if (conn == null) {
                throw new SQLException("no connection");
            }

            resultSet = createResultSet(conn, sql);

            if (resultSet == null) {
                throw new SQLException("no resultSet");
            }

            queueManager.addAllFetchToQueue(resultSet);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                    conn.close();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }


}

