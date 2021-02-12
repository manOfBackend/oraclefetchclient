package Queue;

import org.apache.avro.generic.GenericData;

public class ParquetQueueManager extends QueueManager<GenericData.Record> {
    public static final ParquetQueueManager queueManager = new ParquetQueueManager();
}
