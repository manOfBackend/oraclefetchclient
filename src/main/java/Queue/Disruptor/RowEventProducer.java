package Queue.Disruptor;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;

import java.sql.ResultSet;

public class RowEventProducer {
    private final RingBuffer<RowEvent<ResultSet>> ringBuffer;

    public RowEventProducer(RingBuffer ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    private EventTranslatorOneArg<RowEvent<ResultSet>, ResultSet> TRANSLATOR =
            new EventTranslatorOneArg<RowEvent<ResultSet>, ResultSet>() {
                @Override
                public void translateTo(RowEvent rowEvent, long l, ResultSet resultSet) {
                    rowEvent.setResultSet(resultSet);
                }
            };

    public void onData(ResultSet rs) {
        ringBuffer.publishEvent(TRANSLATOR, rs);
    }
}
