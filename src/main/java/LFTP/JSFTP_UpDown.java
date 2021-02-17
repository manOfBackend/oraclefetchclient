package LFTP;

import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JSFTP_UpDown {
    private String username;
    private String remoteHost;
    private String password;

    //ssh session connect
    public ChannelSftp setupJsch() throws JSchException, IOException {
        JSch jsch = new JSch();
        jsch.setKnownHosts("/Users/home/.ssh/known_hosts");

        BufferedReader reader;
        reader = new BufferedReader(new FileReader(String.valueOf(Paths.get("config.txt"))));
        String line = reader.readLine();
        this.username = line;
        line = reader.readLine();
        this.remoteHost = line;
        line = reader.readLine();
        this.password = line;
        reader.close();
        Session jschSession = jsch.getSession(username, remoteHost);
        jschSession.setPassword(password);
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.connect();
        return (ChannelSftp) jschSession.openChannel("sftp");
    }

    //get size of target file
    public long getSize(String srcFileName) throws JSchException, SftpException, IOException {
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();

        long size = channelSftp.stat(srcFileName).getSize();
        channelSftp.quit();
        return size;
    }

    //get extend of target file
    public String getExtend() throws IOException, JSchException {
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(String.valueOf(Paths.get("config.txt"))));
        String line = null;
        for (int i = 0; i < 4; ++i) {
            line = reader.readLine();
        }
        String ext = line.substring(line.lastIndexOf(".") + 1);
        reader.close();
        return ext;
    }

    //get sections of remote file
    public List<FileChunk> makeFileChunk(int num, String srcFileName) throws IOException, JSchException, SftpException {
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();

        long size = channelSftp.stat(srcFileName).getSize();
        List<FileChunk> list = new ArrayList<>();
        long offset = 0;
        long limit = 0;
        long interval = (long) Math.ceil(size / num);

        for (int i = 0; i < num; i++) {
            limit += interval;
            if (i == num - 1) {
                list.add(new FileChunk(offset, size));
            } else {
                list.add(new FileChunk(offset, limit));
            }
            offset += interval;
        }
        channelSftp.quit();
        return list;
    }

    //Making Threads
    public List<CompletableFuture> addCompletableFuture(int num, List<FileChunk> fileChunkList, List<CompletableFuture> futureList, Path file) {
        ExecutorService service = Executors.newFixedThreadPool(num);
        List<CompletableFuture> list = new ArrayList<>();

        for (int i = 0; i < num; ++i) {
            list.add(CompletableFuture.runAsync(new DownloadThread(i, fileChunkList.get(i).getOffset(), fileChunkList.get(i).getLimit(), file), service));
        }
        return list;
    }

    //upLoad using put
    public void upLoad(String localFile, String remoteDir) throws JSchException, SftpException, IOException {
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();
        channelSftp.put(localFile, remoteDir);
        channelSftp.quit();
    }

    //parallel upload...
    //create empty file & send to remote server
    public void putEmptyFile() throws IOException, JSchException, SftpException {
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();
        //create empty file
        String remoteDir = "/home/fasoo/sftp_dir";
        //String src = "/home/fasoo/IdeaProjects/J_SFTP2/down.txt";
        Path path = Paths.get("upload.txt");
        if (Files.exists(path)) {
            Files.delete(path);
        }
        Path file = Files.createFile(path);

        //send empty file
        channelSftp.put(String.valueOf(file), remoteDir);
        channelSftp.quit();
    }

    //remote file set size
    public void setLength(long srcSize, String file) throws IOException, JSchException, SftpException {
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();
        //set length remote file
        SftpATTRS sftpATTRS = channelSftp.stat(file);
        sftpATTRS.setSIZE(srcSize);
        System.out.println(sftpATTRS.getSize());
        channelSftp.setStat(file, sftpATTRS);
        channelSftp.quit();
    }

    //get sections of local src file
    public List<FileChunk> makeFileChunk1(int threadCount, String srcFileName) {

        //long size = srcFile.length();

        File file = new File(srcFileName);
        long size = file.length();

        List<FileChunk> list = new ArrayList<>();
        long offset = 0;
        long limit = 0;
        long interval = (long) Math.ceil(size / threadCount);
        for (int i = 0; i < threadCount; i++) {
            limit += interval;
            if (i == threadCount - 1) {
                list.add(new FileChunk(offset, size));
            } else {
                list.add(new FileChunk(offset, limit));
            }
            offset += interval;
        }
        for (int i = 0; i < threadCount; ++i) {
            System.out.println(list.get(i).getOffset() + " " + list.get(i).getLimit());
        }
        return list;
    }

}
