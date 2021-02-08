import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class QueueManager {

    private static final BlockingQueue<List<String[]>> queue = new LinkedBlockingQueue<>(100000);

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


    public static void addList(List<String[]> list) {
        queue.add(list);
    }

    public static Optional<List<String[]>> getList() {
        try {
            return Optional.of(queue.poll(TIMEOUT, TIME_UNIT));
        } catch (InterruptedException e) {

        }
        return Optional.empty();
    }
}
