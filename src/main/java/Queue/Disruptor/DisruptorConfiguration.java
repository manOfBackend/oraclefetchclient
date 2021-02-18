package Queue.Disruptor;

import Downloader.Writer.Disruptor.Writer;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class DisruptorConfiguration {
    private DisruptorProperties disruptorProperties;

    public DisruptorConfiguration(DisruptorProperties disruptorProperties) {
        this.disruptorProperties = disruptorProperties;
    }

    private EventFactory<ResultSetEvent> resultSetEventEventFactory() {
        EventFactory<ResultSetEvent> resultSetEventEventFactory = new EventFactory<ResultSetEvent>() {
            @Override
            public ResultSetEvent newInstance() {
                return new ResultSetEvent();
            }
        };
        return resultSetEventEventFactory;
    }

    // Producer와 연결할 RingBuffer setting
    public RingBuffer<ResultSetEvent> run(Writer eventHandler) {
        Disruptor<ResultSetEvent> disruptor = new Disruptor<>(
                resultSetEventEventFactory(),
                disruptorProperties.getRingBufferSize(),
                disruptorProperties.getThreadFactory(),
                disruptorProperties.getProducerType(),
                disruptorProperties.getWaitStartegy()
        );

        disruptor.handleEventsWith(eventHandler);

        RingBuffer<ResultSetEvent> ringBuffer = disruptor.getRingBuffer();
        disruptor.start();

        return ringBuffer;
    }


}