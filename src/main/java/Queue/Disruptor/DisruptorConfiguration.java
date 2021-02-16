package Queue.Disruptor;

import Downloader.Writer.Disruptor.Writer;
import Queue.Disruptor.DisruptorProperties;
import Queue.Disruptor.RowEvent;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class DisruptorConfiguration {
    private DisruptorProperties disruptorProperties;

    public DisruptorConfiguration(DisruptorProperties disruptorProperties) {
        this.disruptorProperties = disruptorProperties;
    }

    private EventFactory<RowEvent<?>> rowEventFactory() {
        EventFactory<RowEvent<?>> rowEventEventFactory = new EventFactory<RowEvent<?>>() {
            @Override
            public RowEvent<?> newInstance() {
                return new RowEvent<>();
            }
        };
        return rowEventEventFactory;
    }

    public RingBuffer<RowEvent<?>> run(Writer eventHandler) {
        Disruptor<RowEvent<?>> disruptor = new Disruptor<>(
                rowEventFactory(),
                disruptorProperties.getRingBufferSize(),
                disruptorProperties.getThreadFactory(),
                disruptorProperties.getProducerType(),
                disruptorProperties.getWaitStartegy()
        );

        disruptor.handleEventsWith(eventHandler);

        RingBuffer<RowEvent<?>> ringBuffer = disruptor.getRingBuffer();
        disruptor.start();

        return ringBuffer;
    }
}