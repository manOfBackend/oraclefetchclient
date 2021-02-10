package Writer;

public abstract class Writer implements Runnable {

    private static String outputPath = "output.csv";

    public static String getOutputPath() {
        return outputPath;
    }

    public static void setOutputPath(String outputPath) {
        Writer.outputPath = outputPath;
    }

}
