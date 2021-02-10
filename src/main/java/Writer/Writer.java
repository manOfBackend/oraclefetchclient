package Writer;

public abstract class Writer implements Runnable {
    private static String outputPath = "output.csv";

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        Writer.outputPath = outputPath;
    }

    protected Writer() {

    }
}
