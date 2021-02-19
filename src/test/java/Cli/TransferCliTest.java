package Cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TransferCliTest {

    @Test
    void oracle_CSV_ParallelCommand_138MB_Test() {
        String[] args = "-o c:\\adid_fetch\\output_adid.csv parallel -sql c:\\adid_fetch\\sql.txt -type CSV -c 1 -host jdbc:oracle:thin:@192.168.80.229:1521:xe -u c##jong -p guswhd12".split(" ");
        int exitCode = new CommandLine(new TransferCli()).execute(args);
        assertEquals(exitCode, 0);
    }

    @Test
    void oracle_CSV_SingleCommand_138MB_Test() {
        String[] args = "-o c:\\adid_fetch\\output_adid.csv single -sql c:\\adid_fetch\\sql.txt --reader-type ORACLE -type CSV -host jdbc:oracle:thin:@192.168.80.229:1521:xe -u c##jong -p guswhd12".split(" ");
        int exitCode = new CommandLine(new TransferCli()).execute(args);
        assertEquals(exitCode, 0);
    }

    @Test
    void oracle_Parquet_SingleCommand_138MB_Test() {
        String[] args = "-o c:\\adid_fetch\\output_adid.csv single --reader-type ORACLE -type PARQUET -o c:\\adid_fetch\\output_adid.parquet -t adid_test -host jdbc:oracle:thin:@localhost:1521:xe -u c##jong -p guswhd12".split(" ");
        int exitCode = new CommandLine(new TransferCli()).execute(args);
        assertEquals(exitCode, 0);
    }

    @Test
    void oracle_Parquet_ParallelCommand_138MB_Test() {
       // String[] args = "-o c:\\adid_fetch\\output_adid.csv parallel --reader-type ORACLE -type PARQUET -o c:\\adid_fetch\\output_adid.parquet -t adid_test -host jdbc:oracle:thin:@localhost:1521:xe -u c##jong -p guswhd12".split(" ");
        String[] args = "-o c:\\adid_fetch\\output_adid.parquet parallel -sql c:\\adid_fetch\\sql.txt -type PARQUET -c 4 -host jdbc:oracle:thin:@localhost:1521:xe -u c##jong -p guswhd12".split(" ");

        int exitCode = new CommandLine(new TransferCli()).execute(args);
        assertEquals(exitCode, 0);
    }

    @Test
    void lftp_CSV_SingleCommand_138MB_Test() {
        String[] args = "lftp -t down -s /home/fasoo/sftp_dir/test_1.2GB.csv -d C:\\Users\\kim0m77\\IdeaProjects\\J_SFTP2\\download.csv -c 4 -u fasoo -host 192.168.21.224 -password thfals12 ".split(" ");
        long beforeTime = System.currentTimeMillis();
        int exitCode = new CommandLine(new TransferCli()).execute(args);
        assertEquals(exitCode, 0);
        long afterTime = System.currentTimeMillis();
        System.out.println((afterTime-beforeTime)/1000);
    }

    @Test
    void lftp_upload_Test(){
        String[] args = "lftp -t up -s C:\\Users\\kim0m77\\IdeaProjects\\J_SFTP2\\up_test.txt -d up_test2.txt -remotedir /home/fasoo/sftp_dir/ -c 4 -u fasoo -host 192.168.21.224 -password thfals12 ".split(" ");
        int exitCode = new CommandLine(new TransferCli()).execute(args);
        assertEquals(exitCode, 0);
    }

    @Test
    void filename_Test() {
        Path of = Path.of("up.txt");
        System.out.println(of.getFileName());
    }

}