package Downloader.Writer;

public enum FileType {
    CSV("CSV"),
    PARQUET("PARQUET"),
    DISRUPTOR_CSV("DISRUPTOR_CSV");

    private final String value;

    FileType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
