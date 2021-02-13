package Reader;

import Queue.QueueManager;

public abstract class Reader implements Runnable {

    // 쿼리당 가져오는 Row 수
    protected int fetchSize = 10;

    protected final String tableName;

    // DB 접속 URL
    protected final String hostName;

    protected final QueueManager<?> queueManager;


    public Reader(int fetchSize, String tableName, String hostName, QueueManager<?> queueManager) {
        this.fetchSize = fetchSize;
        this.tableName = tableName;
        this.hostName = hostName;
        this.queueManager = queueManager;
    }


}
