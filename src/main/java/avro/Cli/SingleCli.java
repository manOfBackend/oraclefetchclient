package avro.Cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name = "single")
public class SingleCli implements Callable<Integer> {

    @ParentCommand
    private TransferCli transferCli;


    @Override
    public Integer call() throws Exception {
        return null;
    }
}
