package Queue.Impl;

import Queue.QueueManager;
import oracle.jdbc.OracleResultSet;
import org.apache.avro.generic.GenericData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CSVQueueManager extends QueueManager<String[]> {
    public static final CSVQueueManager queueManager = new CSVQueueManager();

    @Override
    public void addAllFetchToQueue(ResultSet rs) throws SQLException {
        List<GenericData.Record> list = new ArrayList<>();
        while (rs.next()) {
//            final int n = rs.getMetaData().getColumnCount();
//            final String[] line = new String[n];
//            for (int i = 1; i<=n; i++) {
//                line[i-1] = rs.getString(i);
//            }
            // TODO: 리더 구현
//            GenericRecordBuilder builder = new GenericRecordBuilder(schemaResults.getParsedSchema());
//
//            list.add(line);
//            if (list.size() == fetchSize) {
//                QueueManager.addList(new ArrayList<>(list));
//                list.clear();
//            }
        }
        if (list.size() > 0) {
//            QueueManager.addList(new ArrayList<>(list));
        }

    }
}
