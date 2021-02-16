package Queue.Disruptor;

import com.lmax.disruptor.WaitStrategy;

public class DisruptorProperties {
    private static Integer ringBufferSize;
    private static WaitStrategy waitStartegy;

    public DisruptorProperties(Integer ringBufferSize, WaitStrategy waitStartegy) {
        DisruptorProperties.ringBufferSize = ringBufferSize;
        DisruptorProperties.waitStartegy = waitStartegy;
    }

    public static Integer getRingBufferSize() {
        return ringBufferSize;
    }

    public static void setRingBufferSize(Integer ringBufferSize) {
        DisruptorProperties.ringBufferSize = ringBufferSize;
    }

    public static WaitStrategy getWaitStartegy() {
        return waitStartegy;
    }

    public static void setWaitStartegy(WaitStrategy waitStartegy) {
        DisruptorProperties.waitStartegy = waitStartegy;
    }
}