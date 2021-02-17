package DbManager.Oracle;

public class OracleWorker implements Runnable {

    private int offset;

    private int limit;

    private String executeSql;

    private OracleManager oracleManager;

    public OracleWorker(String executeSql, int offset, int limit, OracleManager oracleManager) {
        this.executeSql = executeSql;
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
    public void run() {

    }
}
