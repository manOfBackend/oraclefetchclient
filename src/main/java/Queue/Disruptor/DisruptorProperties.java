package Queue.Disruptor;

import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.ThreadFactory;

public class DisruptorProperties {
    private static Integer ringBufferSize;
    private static WaitStrategy waitStartegy;
    private static ProducerType producerType;
    private static ThreadFactory threadFactory;

    public DisruptorProperties(Integer ringBufferSize, WaitStrategy waitStartegy, ProducerType producerType, ThreadFactory threadFactory) {
        DisruptorProperties.ringBufferSize = ringBufferSize;
        DisruptorProperties.waitStartegy = waitStartegy;
        DisruptorProperties.producerType = producerType;
        DisruptorProperties.threadFactory = threadFactory;
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

    public static ProducerType getProducerType() {
        return producerType;
    }

    public static void setProducerType(ProducerType producerType) {
        DisruptorProperties.producerType = producerType;
    }

    public static ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public static void setThreadFactory(ThreadFactory threadFactory) {
        DisruptorProperties.threadFactory = threadFactory;
    }
}