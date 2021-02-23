package DbManager.Oracle;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleDriver;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class OracleManager implements Closeable {

    private final OracleConnection connection;

    private final String hostName;

    private final String userName;

    private final String password;

    private final String tempTableName = "TEMP_ADID";

    private static final int EXECUTE_BATCH_SIZE = 500;

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

    public OracleConnection getConnection() {
        return connection;
    }

    private OracleConnection createConnection(String hostName, String userName, String password) throws SQLException {
        final OracleDriver orcDriver = new OracleDriver();
        final Properties properties = new Properties();
        properties.setProperty("user", userName);
        properties.setProperty("password", password);

        final OracleConnection connect = (OracleConnection) orcDriver.connect(hostName, properties);
        connect.setAutoCommit(false);
        connect.setDefaultExecuteBatch(EXECUTE_BATCH_SIZE);
        return connect;
    }


    public void upload(String inputFileName) {

        try (
               // OracleConnection connection = createConnection(hostName, userName, password);
                BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(inputFileName), StandardCharsets.UTF_8)
        ) {
            CSVReader reader = new CSVReader(bufferedReader);

            String[] records = reader.readNext();

            int columnCount = records.length;

            createTempTable(columnCount);

            // create insert into statement
            String insertSql = createInsertStatement(columnCount);

            // insert data into table
            OraclePreparedStatement insertStmt = (OraclePreparedStatement) createOraclePreparedStatement(insertSql);

            int batchCount = 0;
            while ((records = reader.readNext()) != null) {

                for (int i = 0; i < columnCount; i++) {
                    insertStmt.setString(i, records[i]);
                }
                insertStmt.addBatch();
                insertStmt.clearParameters();
                batchCount++;

                if (batchCount == EXECUTE_BATCH_SIZE) {
                    insertStmt.executeBatch();
                    connection.commit();
                    batchCount = 0;
                }

            }

            insertStmt.executeBatch();
            connection.commit();
        }
        catch (SQLException | IOException | CsvValidationException throwables) {
            throwables.printStackTrace();
        }


    }

    private void createTempTable(int columnCount) throws SQLException {
        // create sql statement
        String createSql = createSqlStatement(columnCount);

        // create table
        OraclePreparedStatement createStmt = (OraclePreparedStatement) createOraclePreparedStatement(createSql);
        createStmt.execute();
    }

    private String createInsertStatement(int columnCount) {
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(tempTableName);
        sb.append("VALUES");
        sb.append("(");
        for (int i = 0; i < columnCount; i++) {
            sb.append("?,");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(')');
        return sb.toString();
    }

    private String createSqlStatement(int columnCount) {

        StringBuilder sb = new StringBuilder();

        sb.append("CREATE TABLE ");
        sb.append(tempTableName);
        sb.append("(");
        for (int i = 0; i < columnCount; i++) {
            sb.append("A").append(i + 1).append(" varchar2(255)").append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        return sb.toString();
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


    @Override
    public void close() throws IOException {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
