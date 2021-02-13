package Queue.Impl;

import Queue.QueueManager;
import avro.SchemaGenerator;
import avro.SchemaResults;
import oracle.jdbc.OracleResultSet;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParquetQueueManager extends QueueManager<GenericRecord> {

    private SchemaResults schemaResults;

    private final SchemaGenerator generator = new SchemaGenerator();

    private static final String schemaName = "jong2";

    private static final String namespace = "com.jong2";

    public SchemaResults getSchemaResults() {
        return schemaResults;
    }

    public ParquetQueueManager() {
    }

    @Override
    public void addAllFetchToQueue(ResultSet resultSet) throws SQLException {
        if (schemaResults == null) {
            schemaResults = generator.generateSchema(resultSet, schemaName, namespace);
        }
        List<GenericData.Record> list = new ArrayList<>();
        while (resultSet.next()) {
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
