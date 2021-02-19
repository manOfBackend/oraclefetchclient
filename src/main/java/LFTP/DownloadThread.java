package LFTP;

import com.jcraft.jsch.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadThread implements Runnable {
    private long offset;
    private long limit;
    private int thnum;
    private String dstFileName;
    private String srcFileName;
    private String username;
    private String remoteHost;
    private String password;


    public DownloadThread(long offset, long limit, int thnum, String dstFileName,String srcFileName, String username, String remoteHost, String password) {
        this.offset = offset;
        this.limit = limit;
        this.thnum = thnum;
        this.dstFileName = dstFileName;
        this.srcFileName = srcFileName;
        this.username = username;
        this.remoteHost = remoteHost;
        this.password = password;
    }

    //ssh session connect
    public ChannelSftp setupJsch() throws JSchException {
        JSch jsch = new JSch();
        jsch.setKnownHosts("/Users/home/.ssh/known_hosts");

        Session jschSession = jsch.getSession(username, remoteHost);
        jschSession.setPassword(password);
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.connect();
        return (ChannelSftp) jschSession.openChannel("sftp");
    }


    @Override
    public void run() {
        try {
            sftp_thread();
        } catch (IOException | JSchException | SftpException e) {
            e.printStackTrace();
        }
    }

    public void sftp_thread() throws IOException, JSchException, SftpException {
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();

        //InputStream inputStream = channelSftp.get(src,sftpprogressMonitor,offset);
        InputStream inputStream = null;
        if (thnum == 0) inputStream = channelSftp.get(srcFileName);
        else inputStream = channelSftp.get(srcFileName, null, offset);

        RandomAccessFile raf = new RandomAccessFile(dstFileName, "rw");

        int flag = 0;
        byte[] bytes = new byte[1024*8];
        raf.seek(offset);
        while(flag == 0){
            flag = inputStream.read(bytes,0,bytes.length);
            //inputStream.read() return 0 means reading empty file
            if(flag == 0){
                System.out.println("Source File is Empty File!");
                break;
            }
            else if(flag == -1)break;
            if(offset + flag >= limit){
                raf.write(bytes,0,(int)(limit-offset));
                break;
            }
            //flag만큼 write해야
            raf.write(bytes,0, flag);
            offset += flag;
            flag = 0;
        }
        raf.close();
        inputStream.close();
        channelSftp.quit();
        channelSftp.getSession().disconnect();
    }
}