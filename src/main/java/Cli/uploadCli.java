package Cli;

import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name = "upload")
public class uploadCli implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        //TODO: UPLOAD LOGIC
        System.out.println("upload cli");


        return null;
    }
}
