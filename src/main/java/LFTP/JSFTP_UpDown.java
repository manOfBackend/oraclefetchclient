package LFTP;

import com.jcraft.jsch.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JSFTP_UpDown {
    private String userName;
    private String remoteHost;
    private String password;

    public JSFTP_UpDown(String username, String remoteHost, String password) {
        this.userName = username;
        this.remoteHost = remoteHost;
        this.password = password;
    }

    //ssh session connect
    public ChannelSftp setupJsch() throws JSchException, IOException {
        JSch jsch = new JSch();
        jsch.setKnownHosts("/Users/home/.ssh/known_hosts");

        Session jschSession = jsch.getSession(userName, remoteHost);
        jschSession.setPassword(password);
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.connect();
        return (ChannelSftp) jschSession.openChannel("sftp");
    }

    public void putTest(String srcFileName, String dstFileName,String remoteDir, long offset) throws JSchException, IOException, SftpException {
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();

        InputStream inputStream = null;
        inputStream = new FileInputStream(srcFileName);

//        channelSftp.put(srcFileName, remoteDir);
        //inputStream.skipNBytes(offset);
        OutputStream outputStream = null;
        outputStream = channelSftp.put(dstFileName,null,ChannelSftp.APPEND, 24);


        int flag = 0;
        byte[] bytes = new byte[24];
        inputStream.skipNBytes(12);
        flag = inputStream.read(bytes,0,12);

        outputStream.write(bytes,0,flag);
        channelSftp.quit();
        channelSftp.getSession().disconnect();
    }



    //get size of target file
    public long getSize(String srcFileName) throws JSchException, SftpException, IOException {
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();

        long size = channelSftp.stat(srcFileName).getSize();
        channelSftp.quit();
        channelSftp.getSession().disconnect();
        return size;
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
        channelSftp.getSession().disconnect();
        return list;
    }

    //Making Threads
    public List<CompletableFuture> addCompletableFuture(int num, List<FileChunk> fileChunkList, String dstFileName,String srcFileName) {
        ExecutorService service = Executors.newFixedThreadPool(num);
        List<CompletableFuture> list = new ArrayList<>();

        for (int i = 0; i < num; ++i) {
            list.add(CompletableFuture.runAsync(new DownloadThread(fileChunkList.get(i).getOffset(), fileChunkList.get(i).getLimit(), i, dstFileName, srcFileName, userName, remoteHost, password), service));
        }
        return list;
    }

    //upLoad using put
    public void upLoad(String localFile, String remoteDir) throws JSchException, SftpException, IOException {
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();
        channelSftp.put(localFile, remoteDir);
        channelSftp.quit();
        channelSftp.getSession().disconnect();
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
        channelSftp.getSession().disconnect();
    }

    public OutputStream makeOutputStream(String dstFileName,List<FileChunk> fileChunkList,int threadCount) throws IOException, JSchException, SftpException {
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();

        OutputStream outputStream = null;
        outputStream = channelSftp.put(dstFileName,null,ChannelSftp.OVERWRITE, fileChunkList.get(threadCount).getOffset());
        return outputStream;
    }


    //remote file set size
    public void setLength(String srcFileName, String dstFileName, String remoteDir) throws IOException, JSchException, SftpException {
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();

        //create empty file at local
        Path path = Paths.get(dstFileName);
        if (Files.exists(path)) {
            Files.delete(path);
        }
        Path dstFile = Files.createFile(path);

        //send empty file
        channelSftp.put(String.valueOf(dstFile), remoteDir);

        //get size of src file
        File srcFile = new File(srcFileName);
        long srcSize = srcFile.length();

        //set length of remote file
        SftpATTRS sftpATTRS = channelSftp.stat(remoteDir+dstFileName);
        sftpATTRS.setSIZE(srcSize);
        System.out.println(sftpATTRS.getSize());
        channelSftp.setStat(remoteDir+dstFileName, sftpATTRS);

        channelSftp.quit();
        channelSftp.getSession().disconnect();
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
