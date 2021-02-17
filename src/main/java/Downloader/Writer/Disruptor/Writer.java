package Downloader.Writer.Disruptor;

import Queue.Disruptor.ResultSetEvent;
import com.lmax.disruptor.EventHandler;

public abstract class Writer<T> implements EventHandler<ResultSetEvent> {
    protected String outputPath = "output.csv";
    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }


    public Writer(String outputPath) {
        this.outputPath = outputPath;
    }
}
