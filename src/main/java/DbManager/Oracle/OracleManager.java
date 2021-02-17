package DbManager.Oracle;

import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleDriver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class OracleManager {

    private final OracleConnection connection;

    public OracleManager(String hostName, String userName, String password) throws SQLException {

        connection = createConnection(hostName, userName, password);

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

    public int getTotalRowsCount(String tableName) throws SQLException {
        String sql = String.format("SELECT count(*) AS TOTAL FROM %s", tableName);
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        final ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.getInt("TOTAL");
    }





}
