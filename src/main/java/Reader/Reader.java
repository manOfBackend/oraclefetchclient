package Reader;

import Queue.QueueManager;

public abstract class Reader implements Runnable {

    // 쿼리당 가져오는 Row 수
    protected int fetchSize = 10;

    protected final String tableName;

    // DB 접속 URL
    protected final String hostName;

    protected final String userName;

    protected final String password;

    protected final QueueManager<?> queueManager;


    public Reader(int fetchSize, String tableName, String hostName, String userName, String password, QueueManager<?> queueManager) {
        this.fetchSize = fetchSize;
        this.tableName = tableName;
        this.hostName = hostName;
        this.userName = userName;
        this.password = password;
        this.queueManager = queueManager;
    }


}
