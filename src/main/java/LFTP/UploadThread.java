package LFTP;

import com.jcraft.jsch.*;

import java.io.*;
import java.nio.file.Paths;

public class UploadThread implements Runnable {
    private long offset;
    private long limit;
    private int thnum;
    private String dstFileName;
    private String srcFileName;
    private String userName;
    private String remoteHost;
    private String password;

    public ChannelSftp setupJsch() throws JSchException, IOException {
        JSch jsch = new JSch();
        jsch.setKnownHosts("/Users/home/.ssh/known_hosts");
        Session jschSession = jsch.getSession(userName, remoteHost);
        jschSession.setPassword(password);
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.connect();
        return (ChannelSftp) jschSession.openChannel("sftp");
    }

    public UploadThread(long offset, long limit, int thnum, String dstFileName,String srcFileName, String userName, String remoteHost, String password) {
        this.offset = offset;
        this.limit = limit;
        this.thnum = thnum;
        this.dstFileName = dstFileName;
        this.srcFileName = srcFileName;
        this.userName = userName;
        this.remoteHost = remoteHost;
        this.password = password;
    }

    @Override
    public void run() {
        try {
            sftp_thread();
        } catch (IOException | JSchException | SftpException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sftp_thread() throws IOException, JSchException, SftpException, InterruptedException {
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();
        OutputStream outputStream = null;
        //outputStream = channelSftp.put(dstFileName, null, ChannelSftp.APPEND, offset);
        if(thnum==0) outputStream = channelSftp.put(dstFileName,null,ChannelSftp.APPEND,0);
        else outputStream = channelSftp.put(dstFileName,null,ChannelSftp.APPEND,offset);
        InputStream inputStream = null;
        inputStream = new FileInputStream(srcFileName);
        inputStream.skipNBytes(offset);
        int flag = 0;
        byte[] bytes = new byte[1024 * 8];
        while (flag == 0) {
            flag = inputStream.read(bytes, 0, bytes.length);
            //System.out.println(thnum + ":" + flag);
            if(flag == 0){
                System.out.println("Source File is Empty File!");
                break;
            }
            else if (flag == -1) break;
            if (offset + flag >= limit) {
                //System.out.println(thnum + ":" + (limit - offset));
                outputStream.write(bytes, 0, (int) (limit - offset));
                break;
            }
            //System.out.println(flag);
            outputStream.write(bytes, 0, flag);
            offset += flag;
            flag = 0;
        }
        inputStream.close();
        outputStream.close();
        channelSftp.quit();
        channelSftp.getSession().disconnect();
    }
}
