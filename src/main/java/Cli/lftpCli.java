package Cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name = "lftp")
public class lftpCli implements Callable<Integer> {


    @Override
    public Integer call() throws Exception {
        //TODO: lftp logic

        System.out.println("lftp cli");
        return 0;
    }
}
