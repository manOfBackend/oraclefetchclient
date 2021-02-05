import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleDriver;

import java.sql.*;
import java.util.Properties;
import java.util.stream.IntStream;

public class Reader {

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
    private void writeToCsv(OracleResultSet rs) throws SQLException {

        while (rs.next()) {
            final int n = rs.getMetaData().getColumnCount();
            final String[] line = IntStream.range(1, n).mapToObj(i -> {
                try {
                    return rs.getString(i);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    return new String[]{};
                }
            }).toArray(size -> new String[size]);

            QueueManager.addLine(line);
        }

    }

    public void run() throws InterruptedException {

        final Thread readerThread = new Thread("Reader") {
            public void run() {
                try {

                    final OracleConnection conn = createConnection(hostName);

                    final String sql = "SELECT * FROM " + tableName;

                    System.out.println("Running: " + sql);

                    final OracleResultSet result = createResultSet(conn, sql);


                    //하이브에 벡터라이즈 엑시큐트 라는 게 있다. 벡터라이제이션
                    //하이브에서 벡터라이즈할 수 있나 저장할 때도 파티셔닝할 수 있나
                    //멀티세션도 답인데 이게 위험한 이유는
                    // 두 세션이 열어서 쿼리 날렸을 때 중복이 발생하기 때문에
                    // 하이브 인덱스 알아볼 것
                    // 인덱스가 있어야 한다.
                    // 인덱스가 있어야 빨리 가능하다.
                    // hive optimization 으로 찾아볼 것
                    //
                    // result.setFetchSize(100);

                    writeToCsv(result);

                    // logic(result);

                    result.close();
                    statement.close();
                    connection.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                    return;
                }
            }
        };

        long startTime = System.currentTimeMillis();
        readerThread.start();

        Thread writerThread = new Thread(Writer.getInstance());
        writerThread.start();

        readerThread.join();
        writerThread.interrupt();
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(estimatedTime / 1000.0);
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
