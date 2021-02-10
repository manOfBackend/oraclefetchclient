import org.apache.avro.generic.GenericData;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueManager {

    private static final BlockingQueue<List<GenericData.Record>> queue = new LinkedBlockingQueue<>(1000);
//    private static final Queue<List<String[]>> queue = new LinkedList<>();


    // 큐가 비어있으면 대기하는 최대 시간
    private static final int TIMEOUT = 10;



    // 대기하는 시간 단위
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private static class QueueManagerHolder {

        private static final QueueManager queueManager = new QueueManager();
    }

    private static QueueManager getInstance() {
        return QueueManagerHolder.queueManager;
    }


    public static void addList(List<GenericData.Record> list) {
        queue.add(list);
    }

    public static Optional<List<GenericData.Record>> getList() throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger(3);
        atomicInteger.incrementAndGet();

        return Optional.ofNullable(queue.poll(TIMEOUT, TIME_UNIT));

    }
}
