package Queue.Impl;

import Queue.QueueManager;
import avro.ResultSetTransformer;
import avro.Schema.SchemaGenerator;
import avro.Schema.SchemaResults;
import avro.Schema.SchemaSqlMapping;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParquetQueueManager extends QueueManager<GenericRecord> {

    private SchemaResults schemaResults;

    private final SchemaGenerator generator = new SchemaGenerator();

    private final String schemaName;

    private final String namespace;

    private final ResultSetTransformer transformer;

    public SchemaResults getSchemaResults() {
        return schemaResults;
    }

    public ParquetQueueManager(ResultSetTransformer transformer, String schemaName, String namespace) {
        this.transformer = transformer;
        this.schemaName = schemaName;
        this.namespace = namespace;
    }

    @Override
    public void addAllFetchToQueue(ResultSet resultSet) throws SQLException {
        if (schemaResults == null) {
            schemaResults = generator.generateSchema(resultSet, schemaName, namespace);
        }
        GenericRecordBuilder builder = new GenericRecordBuilder(schemaResults.getParsedSchema());
        List<GenericRecord> list = new ArrayList<>();

        while (resultSet.next()) {

            for (SchemaSqlMapping mapping : schemaResults.getMappings()) {

                builder.set(schemaResults.getParsedSchema().getField(mapping.getSchemaName()),
                        transformer.extract(mapping, resultSet));
            }

            GenericRecord record = builder.build();

            list.add(record);
            if (list.size() == resultSet.getFetchSize()) {
                addList(new ArrayList<>(list));
                list.clear();
            }
        }
        if (list.size() > 0) {
            addList(new ArrayList<>(list));
        }
    }
}
