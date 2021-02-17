package LFTP;

import com.jcraft.jsch.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadThread implements Runnable {
    private long offset;
    private long limit;
    private int thnum;
    private Path file;
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

    public DownloadThread(int th_num, long offset, long limit, Path file) {
        this.offset = offset;
        this.limit = limit;
        this.thnum = th_num;
        this.file = file;
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

        //read config file
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(String.valueOf(Paths.get("config.txt"))));
        String line = null;
        for (int i = 0; i < 4; ++i) {
            line = reader.readLine();
        }
        String src = line;
        reader.close();

        //InputStream inputStream = channelSftp.get(src,sftpprogressMonitor,offset);
        InputStream inputStream = null;
        if (thnum == 0) inputStream = channelSftp.get(src);
        else inputStream = channelSftp.get(src, null, offset);

        RandomAccessFile raf = new RandomAccessFile(String.valueOf(file), "rw");

        int flag = 0;
        while (flag == 0) {
            byte[] bytes = new byte[1024 * 8];
            flag = inputStream.read(bytes, 0, bytes.length);
            //System.out.println(thnum + ":" + flag);
            if (flag == -1) break;
            //offset + flag 값이 limit와 같을 때, > 조건문에 걸리지 않고 넘어가서 쓰레기값을 저장 할 수 있음, >=필요
            if (offset + flag >= limit) {
                raf.seek(offset);
                raf.write(bytes, 0, (int) (limit - offset));
                break;
            }
            raf.seek(offset);
            raf.write(bytes, 0, bytes.length);
            offset += flag;
            flag = 0;
        }
        raf.close();
        inputStream.close();
        channelSftp.quit();
    }
}