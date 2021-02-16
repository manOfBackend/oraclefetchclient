package Downloader.Writer.Disruptor;

import Queue.BlockingQueue.QueueManager;
import Queue.Disruptor.RowEvent;
import com.lmax.disruptor.EventHandler;

public abstract class Writer<T> implements EventHandler<RowEvent<T>> {
    protected String outputPath = "output.csv";
    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    protected final RowEvent<T> rowEvent;
    public RowEvent<T> getRowEvent() {
        return rowEvent;
    }


    public Writer(String outputPath, RowEvent<T> rowEvent) {
        this.outputPath = outputPath;
        this.rowEvent = rowEvent;

    }
}
