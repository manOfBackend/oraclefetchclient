package Cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import static org.junit.jupiter.api.Assertions.*;

class TransferCliTest {

    @Test
    void oracle_CSV_ParallelCommand_138MB_Test() {
        String[] args = "-o c:\\adid_fetch\\output_adid.csv parallel --reader-type ORACLE -type CSV -t adid_test -host jdbc:oracle:thin:@localhost:1521:xe -u c##jong -p guswhd12".split(" ");
        int exitCode = new CommandLine(new TransferCli()).execute(args);
        assertEquals(exitCode, 0);
    }

    @Test
    void oracle_CSV_SingleCommand_138MB_Test() {
        String[] args = "-o c:\\adid_fetch\\output_adid.csv single --reader-type ORACLE -type CSV -t adid_test -host jdbc:oracle:thin:@localhost:1521:xe -u c##jong -p guswhd12".split(" ");
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
        String[] args = "-o c:\\adid_fetch\\output_adid.csv parallel --reader-type ORACLE -type PARQUET -o c:\\adid_fetch\\output_adid.parquet -t adid_test -host jdbc:oracle:thin:@localhost:1521:xe -u c##jong -p guswhd12".split(" ");
        int exitCode = new CommandLine(new TransferCli()).execute(args);
        assertEquals(exitCode, 0);
    }


}