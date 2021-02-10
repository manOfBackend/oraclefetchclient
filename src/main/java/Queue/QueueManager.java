package Queue;

import org.apache.avro.generic.GenericData;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueManager<T> {

    private final BlockingQueue<List<T>> queue = new LinkedBlockingQueue<>(1000);
//    private static final Queue<List<String[]>> queue = new LinkedList<>();


    // 큐가 비어있으면 대기하는 최대 시간
    private static final int TIMEOUT = 10;



    // 대기하는 시간 단위
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    public static <T> T getInstance(Class<T> t) throws IllegalAccessException, InstantiationException {

        return t.newInstance();

    }


    public void addList(List<T> list) {
        queue.add(list);
    }

    public Optional<List<T>> getList() throws InterruptedException {

        return Optional.ofNullable(queue.poll(TIMEOUT, TIME_UNIT));

    }
}
