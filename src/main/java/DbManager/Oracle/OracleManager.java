package DbManager.Oracle;

import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleDriver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class OracleManager {

    private final OracleConnection connection;

    private final String hostName;

    private final String userName;

    private final String password;

    public OracleManager(String hostName, String userName, String password) throws SQLException {
        this.hostName = hostName;
        this.userName = userName;
        this.password = password;
        connection = createConnection(this.hostName, this.userName, this.password);

    }

    public String getHostName() {
        return hostName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public OracleConnection createConnection(String hostName, String userName, String password) throws SQLException {
        final OracleDriver orcDriver = new OracleDriver();
        final Properties properties = new Properties();
        properties.setProperty("user", userName);
        properties.setProperty("password", password);

        final OracleConnection connect = (OracleConnection) orcDriver.connect(hostName, properties);
        connect.setAutoCommit(false);
        connect.setDefaultExecuteBatch(500);
        return connect;
    }

    public PreparedStatement createOraclePreparedStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    public ResultSet getResultSet(String sql, int fetchSize) throws SQLException {
        final PreparedStatement preparedStatement = createOraclePreparedStatement(sql);
        preparedStatement.setFetchSize(fetchSize);
        return preparedStatement.executeQuery();
    }

    public int getTotalRowsCount(String executeSql) throws SQLException {
        String sql = String.format("SELECT count(*) AS TOTAL FROM (%s)", executeSql);
        final PreparedStatement preparedStatement = createOraclePreparedStatement(sql);
        final ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getInt("TOTAL");
    }





}
