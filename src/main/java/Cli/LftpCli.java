package Cli;

import LFTP.FileChunk;
import LFTP.JSFTP_UpDown;
import LFTP.UploadThread;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.ExecutionException;

import static picocli.CommandLine.*;
import static picocli.CommandLine.Command;

@Command(name = "lftp")
public class LftpCli implements Callable<Integer> {

    @Option(names = {"-t", "--trans-type"}, required = true)
    private String transType;

    /*
            String src = "/home/fasoo/IdeaProjects/J_SFTP2/down.txt";
        String dst = "/home/fasoo/sftp_dir";
     */
    @Option(names = {"-s", "--src"}, required = true)
    private String srcFileName;

    @Option(names = {"-d", "--dst"}, required = false)
    private String dstFileName;

    @Option(names = {"-u", "--user-name"}, required = true)
    private String userName;

    @Option(names = {"-host", "--remote-host"}, required = true)
    private String remoteHost;

    @Option(names = {"-password", "--password"}, required = true)
    private String password;

    @Option(names = {"-c", "--thread-count"}, required = true)
    private int threadCount;

    @Option(names = {"-remotedir", "--remotedir"}, required = false)
    private String remoteDir;

    private void upload(JSFTP_UpDown JS1) throws IOException, JSchException, SftpException {
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(String.valueOf(Paths.get("up.txt"))));
        String localFile = reader.readLine();
        String remoteDir = reader.readLine();
        reader.close();
        JS1.upLoad(localFile, remoteDir);
        System.out.println("upload finish");
    }

    private void up(JSFTP_UpDown JS1) throws IOException, JSchException, SftpException {

        //create empty file & put, set length remote file
        JS1.setLength(srcFileName, dstFileName, remoteDir);

        //get sections of src file
        List<FileChunk> fileChunkList = new ArrayList<>();
        fileChunkList = JS1.makeFileChunk1(threadCount, srcFileName);

        //making uploadThread & executing
        MakingThreads_up(fileChunkList);
    }

    private void MakingThreads_up(List<FileChunk> fileChunkList) {
        ExecutorService service = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture> futureList = new ArrayList<>();
        for (int i = 0; i < threadCount; ++i) {
            futureList.add(CompletableFuture.runAsync(new UploadThread(fileChunkList.get(i).getOffset(), fileChunkList.get(i).getLimit(), i, remoteDir+dstFileName, srcFileName, userName, remoteHost, password), service));
        }
        CompletableFuture<Void> futureAll = CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new)).thenRun(() -> {
            System.out.println("finish");
        });
        try {
            futureAll.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        service.shutdownNow();
    }


    @Override
    public Integer call() {
        //TODO: lftp logic

        System.out.println("lftp cli");

        JSFTP_UpDown JS1 = new JSFTP_UpDown(userName, remoteHost, password);

        try {
            switch (transType) {
                case "download", "down" -> download(JS1);
                case "upload" -> upload(JS1);
                case "up" -> up(JS1);
            }
        } catch (IOException | JSchException | SftpException ioException) {
            ioException.printStackTrace();
        }

        return 0;
    }

    private void download(JSFTP_UpDown JS1) throws IOException, JSchException, SftpException {

        List<FileChunk> fileChunkList = new ArrayList<>();
        fileChunkList = JS1.makeFileChunk(threadCount, srcFileName);
        PrintFileChunks(threadCount, fileChunkList);

        //make dstFile & dstFile size allocate
        FileSizeAllocate(JS1.getSize(srcFileName));

        //making downloadThread & executing
        MakingThreads_down(JS1, threadCount, fileChunkList);

    }

    private void FileSizeAllocate(long size) throws IOException {
        Path path = Path.of(dstFileName);
        if (Files.exists(path)) {
            Files.delete(path);
        }
        Path file = Files.createFile(path);
        RandomAccessFile raf = new RandomAccessFile(String.valueOf(file), "rw");
        raf.setLength(size);
        raf.close();
    }

    private void MakingThreads_down(JSFTP_UpDown JS1, int num, List<FileChunk> fileChunkList) {
        ExecutorService service = Executors.newFixedThreadPool(num);
        List<CompletableFuture> futureList = new ArrayList<>();

        futureList = JS1.addCompletableFuture(num, fileChunkList, dstFileName, srcFileName);

        CompletableFuture<Void> futureAll = CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new)).thenRun(() -> {
            System.out.println("Download finish");
        });
        try {
            futureAll.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        service.shutdownNow();
    }

    private void PrintFileChunks(int num, List<FileChunk> fileChunkList) {
        for (int i = 0; i < num; ++i) {
            System.out.println(fileChunkList.get(i).getOffset() + " " + fileChunkList.get(i).getLimit());
        }
    }

    public String getHash(String path) throws IOException, NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        FileInputStream fileInputStream = new FileInputStream(path);
        byte[] dataBytes = new byte[1024];
        int nRead = 0;
        while ((nRead = fileInputStream.read(dataBytes)) != -1) {
            messageDigest.update(dataBytes, 0, nRead);
        }
        byte[] mdBytes = messageDigest.digest();
        StringBuilder stringBuffer = new StringBuilder();
        for (byte mdByte : mdBytes) {
            stringBuffer.append(Integer.toString((mdByte & 0xff) + 0x100, 16)).substring(1);
        }
        return stringBuffer.toString();
    }
}
