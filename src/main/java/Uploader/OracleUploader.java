package Uploader;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleDriver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class OracleUploader implements Runnable {

    private final String insertSql;

    private final String createSql;

    private final String hostName;

    private final String userName;

    private final String password;

    private final String inputFileName;

    public OracleUploader(String createSql, String insertSql, String hostName, String inputFileName, String userName, String password) {
        this.createSql = createSql;
        this.insertSql = insertSql;
        this.hostName = hostName;
        this.userName = userName;
        this.password = password;
        this.inputFileName = inputFileName;

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

    @Override
    public void run() {

        System.out.println("Running: " + insertSql);

        try (
                OracleConnection conn = createConnection(hostName, userName, password);
                OraclePreparedStatement oracleStmt = (OraclePreparedStatement) conn.prepareStatement(insertSql);
                BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(inputFileName), StandardCharsets.UTF_8)
        ) {
            conn.setAutoCommit(false);
            conn.setDefaultExecuteBatch(500);

            CSVReader reader = new CSVReader(bufferedReader);

            String[] records = null;

            while ((records = reader.readNext()) != null) {
                /*
                sql_statement.setString(1, nextLine[0]);
                sql_statement.setDouble(2,Double.parseDouble(nextLine[1]));
                // Add the record to batch
                sql_statement.addBatch();
                 */
                oracleStmt.addBatch();
            }

            long[] batchs = oracleStmt.executeLargeBatch();

            conn.commit();
        } catch (SQLException | IOException | CsvValidationException throwables) {
            throwables.printStackTrace();
        }


    }
}
