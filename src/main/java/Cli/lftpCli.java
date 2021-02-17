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
public class lftpCli implements Callable<Integer> {

    @Option(names = {"-t", "--trans-type"}, required = true)
    private String transType;

    /*
            String src = "/home/fasoo/IdeaProjects/J_SFTP2/down.txt";
        String dst = "/home/fasoo/sftp_dir";
     */
    @Option(names = {"-s", "--src"}, required = true)
    private File srcFile;

    @Option(names = {"-d", "--dst"}, required = true)
    private File dst;

    @Option(names = {"-c", "--thread-count"}, required = true)
    private int threadCount;

    private void PrintFileChunks(int num, List<FileChunk> fileChunkList) {
        for (int i = 0; i < num; ++i) {
            System.out.println(fileChunkList.get(i).getOffset() + " " + fileChunkList.get(i).getLimit());
        }

    }

    private Path createFile(String ext) throws IOException {
        Path path = Paths.get("download" + "." + ext);
        if (Files.exists(path)) {
            Files.delete(path);
        }
        Path file = Files.createFile(path);
        return file;
    }

    private void FileSizeAllocate(long size, Path file) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(String.valueOf(file), "rw");
        raf.setLength(size);
        raf.close();
    }

    private void MakingThreads(JSFTP_UpDown JS1, int num, List<FileChunk> fileChunkList, Path file) {
        ExecutorService service = Executors.newFixedThreadPool(num);
        List<CompletableFuture> futureList = new ArrayList<>();

        futureList = JS1.addCompletableFuture(num, fileChunkList, futureList, file);

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

    private void upload(JSFTP_UpDown JS1) throws IOException, JSchException, SftpException {
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(String.valueOf(Paths.get("up.txt"))));
        String localFile = reader.readLine();
        String remoteDir = reader.readLine();
        reader.close();
        JS1.upLoad(localFile, remoteDir);
        System.out.println("upload finish");
    }

    private void up(JSFTP_UpDown JS1, int num) throws IOException, JSchException, SftpException {

        //send empty file
        JS1.putEmptyFile();

        //get size of src file
        long size = srcFile.length();

        //set length remote file
        JS1.setLength(size, "/home/fasoo/sftp_dir/upload.txt");

        //get sections of src file
        List<FileChunk> list = new ArrayList<>();
        list = JS1.makeFileChunk1(num, srcFile);

        //Making Threads
        ExecutorService service = Executors.newFixedThreadPool(num);
        List<CompletableFuture> futureList = new ArrayList<>();
        for (int i = 0; i < num; ++i) {
            futureList.add(CompletableFuture.runAsync(new UploadThread(i, list.get(i).getOffset(), list.get(i).getLimit(), srcFile.toPath()), service));
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


    @Override
    public Integer call() {
        //TODO: lftp logic

        System.out.println("lftp cli");

        JSFTP_UpDown JS1 = new JSFTP_UpDown();

        try {
            switch (transType) {
                case "download", "down" -> {
                    List<FileChunk> fileChunkList = new ArrayList<>();
                    fileChunkList = JS1.makeFileChunk(threadCount);
                    PrintFileChunks(threadCount, fileChunkList);
                    long size = JS1.getSize();
                    String ext = JS1.getExtend();
                    Path file = createFile(ext);
                    FileSizeAllocate(size, file);
                    MakingThreads(JS1, threadCount, fileChunkList, file);
                }
                case "upload" -> upload(JS1);
                case "up" -> up(JS1, threadCount);
            }
        } catch (IOException | JSchException | SftpException ioException) {
            ioException.printStackTrace();
        }

        return 0;
    }
}
