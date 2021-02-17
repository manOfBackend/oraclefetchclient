package Queue.Disruptor;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;

import java.sql.ResultSet;

public class ResultSetEventProducer {
    private final RingBuffer<ResultSetEvent> ringBuffer;

    public ResultSetEventProducer(RingBuffer ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    private EventTranslatorOneArg<ResultSetEvent, ResultSet> TRANSLATOR =
            new EventTranslatorOneArg<ResultSetEvent, ResultSet>() {
                @Override
                public void translateTo(ResultSetEvent resultSetEvent, long l, ResultSet resultSet) {
                    resultSetEvent.setResultSet(resultSet);
                }
            };

    public void onData(ResultSet rs) {
        ringBuffer.publishEvent(TRANSLATOR, rs);
    }
}
