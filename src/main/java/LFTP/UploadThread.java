package LFTP;

import com.jcraft.jsch.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UploadThread implements Runnable {
    private long offset;
    private long limit;
    private int thnum;
    private Path file;
    private String username;
    private String remoteHost;
    private String password;

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

    public UploadThread(int th_num, long offset, long limit, Path file) {
        this.offset = offset;
        this.limit = limit;
        this.thnum = th_num;
        this.file = file;
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
        String dst = "/home/fasoo/sftp_dir/upload.txt";
        OutputStream outputStream = null;
        //System.out.println(thnum + " " + offset);
        outputStream = channelSftp.put(dst, null, ChannelSftp.APPEND, offset);
//        if(thnum==0) outputStream = channelSftp.put(dst,null,ChannelSftp.APPEND,0);
//        else outputStream = channelSftp.put(dst,null,ChannelSftp.APPEND,offset);
        InputStream inputStream = null;
        inputStream = new FileInputStream(String.valueOf(file));
        inputStream.skipNBytes(offset);
        int flag = 0;
        while (flag == 0) {
            byte[] bytes = new byte[1024 * 8];
            flag = inputStream.read(bytes, 0, bytes.length);
            //System.out.println(thnum + ":" + flag);
            if (flag == -1) break;
            if (offset + flag >= limit) {
                System.out.println(thnum + ":" + (limit - offset));
                outputStream.write(bytes, 0, (int) (limit - offset));

                break;
            }
            System.out.println(flag);
            outputStream.write(bytes, 0, bytes.length);
            offset += flag;
            flag = 0;
        }
        inputStream.close();
        outputStream.close();
        channelSftp.quit();
    }
}
