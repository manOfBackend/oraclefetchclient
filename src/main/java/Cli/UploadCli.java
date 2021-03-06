package Cli;

import DbManager.Oracle.OracleManager;
import Uploader.OracleUploader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

/**
 * CSV TO ORACLE, CSV TO HIVE
 * PARQUET TO ORACLE, PARQUET TO HIVE
 */
@Command(name = "upload")
public class UploadCli implements Callable<Integer> {

    @Option(names = {"-c", "--create-sql-file"}, required = true)
    private File createSqlFile;

    @Option(names = {"-i", "--insert-sql-file"}, required = true)
    private File insertSqlFile;

    @Option(names = {"-f", "--input-file-name"}, required = true)
    private File inputFile;

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

        final String createSql = Files.readString(createSqlFile.toPath());
        final String insertSql = Files.readString(insertSqlFile.toPath());
        final String inputFileName = inputFile.getPath();

//        Thread uploadThread = new Thread(new OracleUploader(createSql, insertSql, hostName, inputFileName, userName, password));
        Thread uploadThread = new Thread(() -> {
            try {
                try(OracleManager oracleManager = new OracleManager(hostName, userName, password)) {
                    oracleManager.upload(inputFileName);
                }
            } catch (SQLException | IOException throwables) {
                throwables.printStackTrace();
            }
        });

        uploadThread.start();
        uploadThread.join();

        return 0;
    }
}
