import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleDriver;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Reader implements Runnable {

    // 쿼리당 가져오는 Row 수
    private int fetchSize = 10;

    private String tableName;

    // DB 접속 URL
    private String hostName;

    public Reader(int fetchSize, String tableName, String hostName) {
        this.fetchSize = fetchSize;
        this.tableName = tableName;
        this.hostName = hostName;
    }

    /**
     * ResultSet이 가져온 쿼리 결과를 CSV에 저장하는 함수(기본 저장 경로: output.csv)
     *
     * @param rs
     * @throws SQLException
     */
    private void addAllToQueue(OracleResultSet rs) throws SQLException {

        List<String[]> list = new ArrayList<>();
        while (rs.next()) {
            final int n = rs.getMetaData().getColumnCount();
            final String[] line = new String[n];
            for (int i = 1; i<=n; i++) {
                line[i-1] = rs.getString(i);
            }
            list.add(line);
            if (list.size() == fetchSize) {
                QueueManager.addList(new ArrayList<>(list));
                list.clear();
            }
        }
        if (list.size() > 0) {
            QueueManager.addList(new ArrayList<>(list));
        }
    }

    @Override
    public void run() {
        final String sql = "SELECT * FROM " + tableName;
        System.out.println("Running: " + sql);

        OracleConnection conn = null;
        OracleResultSet resultSet = null;
        try {
            conn = createConnection(hostName);
            resultSet = createResultSet(conn, sql);
            addAllToQueue(resultSet);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        finally {
            try {
                resultSet.close();
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    private OracleResultSet createResultSet(OracleConnection conn, String sql) throws SQLException {
        String SQL = "select * from adid_test";


        final OraclePreparedStatement oracleStmt = (OraclePreparedStatement) conn.prepareStatement(SQL, OracleResultSet.FETCH_FORWARD,
                OracleResultSet.CONCUR_READ_ONLY, OracleResultSet.TYPE_FORWARD_ONLY);


        return (OracleResultSet) oracleStmt.getReturnResultSet();
    }

    private OracleConnection createConnection(String hostName) throws SQLException {
        final OracleDriver orcDriver = new OracleDriver();
        final Properties properties = new Properties();
        properties.setProperty("user", "c##jong");
        properties.setProperty("password", "guswhd12");

        final OracleConnection conn = (OracleConnection) orcDriver.connect(hostName, properties);

        return conn;
    }

    public static void main(String[] args) {

    }
}
