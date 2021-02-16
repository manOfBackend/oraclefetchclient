package Downloader.Writer;

import Queue.BlockingQueue.QueueManager;

public abstract class Writer implements Runnable {

    protected String outputPath = "output.csv";

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    protected final QueueManager<?> queueManager;

    public QueueManager<?> getQueueManager() {
        return queueManager;
    }

    public Writer(String outputPath, QueueManager<?> queueManager) {
        this.outputPath = outputPath;
        this.queueManager = queueManager;

    }
}
