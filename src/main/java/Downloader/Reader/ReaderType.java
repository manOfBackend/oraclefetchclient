package Downloader.Reader;

public enum ReaderType {
    ORACLE("ORACLE"),
    HIVE("HIVE");

    private final String value;

    ReaderType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
