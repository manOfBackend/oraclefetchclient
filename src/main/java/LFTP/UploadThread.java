package LFTP;

import com.jcraft.jsch.*;
import net.schmizz.sshj.sftp.RemoteFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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

        InputStream inputStream = null;
        inputStream = new FileInputStream(srcFileName);
        inputStream.skipNBytes(offset);

        OutputStream outputStream = null;
        if(thnum==0) outputStream = channelSftp.put(dstFileName,null,ChannelSftp.OVERWRITE,0);
        else outputStream = channelSftp.put(dstFileName,null,ChannelSftp.RESUME,offset);

        int flag = 0;
        byte[] bytes = new byte[1024 * 8];
        while (flag == 0) {
            flag = inputStream.read(bytes, 0, bytes.length);
            if(flag == 0){
                System.out.println("Source File is Empty File!");
                break;
            }
            else if (flag == -1) break;
            if (offset + flag >= limit) {
                outputStream.write(bytes, 0, (int) (limit - offset));
                break;
            }
            outputStream.write(bytes, 0, flag);
            offset += flag;
            flag = 0;
        }
        inputStream.close();
        outputStream.close();
        channelSftp.quit();
        channelSftp.getSession().disconnect();
        //        FileOutputStream fileOutputStream = new FileOutputStream(String.valueOf(channelSftp.put(dstFileName)));
//        FileChannel fileChannel = fileOutputStream.getChannel();
//        fileChannel.position(offset);
//        ByteBuffer byteBuffer = ByteBuffer.allocate(1024*8);
//        int flag = 0;
//        byte[] bytes = new byte[1024 * 8];
//        while(flag==0){
//            byteBuffer.put((byte) inputStream.read(bytes,0,bytes.length));
//            flag = byteBuffer.remaining();
//            System.out.println(flag);
//            if(flag==0)break;
//            if(offset+flag>=limit){
//                //byteBuffer.clear();
//                byteBuffer.flip();
//                byteBuffer.get(bytes,0,byteBuffer.remaining());
//                byteBuffer.clear();
//                byteBuffer.put(bytes,0,(int)(limit-offset));
//                //fileChannel.write(byteBuffer,(int)offset,(int)(limit-offset));
//                //byteBuffer.flip();
//                fileChannel.write(byteBuffer);
//            }
//            byteBuffer.flip();
//            fileChannel.write(byteBuffer);
//            byteBuffer.clear();
//            offset+=flag;
//            flag=0;
//        }
    }
}
