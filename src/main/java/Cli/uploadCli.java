package Cli;

import Uploader.OracleUploader;

import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name = "upload")
public class uploadCli implements Callable<Integer> {
    @Option(names = {"-c", "--create-sql-file-name"}, required = true)
    private String createSqlFileName;

    @Option(names = {"-t", "--insert-sql-file-name"}, required = true)
    private String insertSqlFileName;

    @Option(names = {"-host", "--host-name"}, required = true)
    private String hostName;

    @Option(names = {"-u", "--user-name"}, required = true)
    private String userName;

    @Option(names = {"-p", "--password"}, required = true)
    private String password;

    @Override
    public Integer call() throws Exception {
        //TODO: UPLOAD LOGIC
        System.out.println("upload cli");

        Thread uploadThread = new Thread(new OracleUploader("","", "", "", "", ""));

        uploadThread.start();
        uploadThread.join();

        return 0;
    }
}
