package avro.Cli;

import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name = "parallel")
public class ParallelCli implements Callable<Integer> {

    @ParentCommand
    private TransferCli transferCli;

    @Option(names = {"-c", "--thread-count"}, defaultValue = "4")
    private int threadCount;


    @Override
    public Integer call() throws Exception {
        return null;
    }
}
