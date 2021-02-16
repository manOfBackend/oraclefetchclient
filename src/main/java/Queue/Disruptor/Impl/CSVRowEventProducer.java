package Queue.Disruptor.Impl;

import Queue.Disruptor.RowEvent;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;

public class CSVRowEventProducer {
    private final RingBuffer<RowEvent<String[]>> ringBuffer;

    public CSVRowEventProducer(RingBuffer<RowEvent<String[]>> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    private EventTranslatorOneArg<RowEvent<String[]>, String[]> TRANSLATOR =
            new EventTranslatorOneArg<RowEvent<String[]>, String[]>() {
                @Override
                public void translateTo(RowEvent rowEvent, long l, String[] strings) {
                    rowEvent.setRow(strings);
                }
            };

    public void onData(String[] row) {
        ringBuffer.publishEvent(TRANSLATOR, row);
    }
}
