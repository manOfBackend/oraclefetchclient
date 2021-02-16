package Queue.Disruptor;

import Downloader.Writer.Disruptor.Impl.CSVWriter;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

public class DisruptorConfiguration {
    private DisruptorProperties disruptorProperties;

    public DisruptorConfiguration(DisruptorProperties disruptorProperties) {
        this.disruptorProperties = disruptorProperties;
    }

    public EventFactory<RowEvent<?>> rowEventFactory() {
        EventFactory<RowEvent<?>> rowEventEventFactory = new EventFactory<RowEvent<?>>() {
            @Override
            public RowEvent<?> newInstance() {
                return new RowEvent<>();
            }
        };
        return rowEventEventFactory;
    }

    public RingBuffer<RowEvent<?>> ringBuffer(EventFactory<RowEvent<?>> eventFactory, WaitStrategy waitStrategy) {
        Disruptor<RowEvent<?>> disruptor = null;

        disruptor = new Disruptor<>(
                rowEventFactory(),
                disruptorProperties.getRingBufferSize(),
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                disruptorProperties.getWaitStartegy()
        );

        return null;
    }

}