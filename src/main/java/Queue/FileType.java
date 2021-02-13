package Queue;

public enum FileType {
    CSV("CSV"),
    PARQUET("PARQUET");

    private final String value;

    FileType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
