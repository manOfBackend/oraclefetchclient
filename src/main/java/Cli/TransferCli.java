package Cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

/**
 * main cli
 */
@Command(description = "ADID Transfer", name = "transfer", mixinStandardHelpOptions = true, version = "transfer 1.0",
        subcommands =
                {
                        SingleCli.class,
                        ParallelCli.class,
                        UploadCli.class,
                        LftpCli.class

                })
public class TransferCli implements Callable<Integer> {

    @Option(names = {"-o", "--output"}, defaultValue = "output_adid")
    private String outputFileName;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new TransferCli()).execute(args);
        System.exit(exitCode);
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("main cli");
        return 0;
    }
}

