package avro;

import org.apache.avro.Schema;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SchemaGenerator {

    public SchemaResults generateSchema(ResultSet resultSet, String name, String nameSpace) throws SQLException {

        SchemaResults schemaResults = new SchemaResults();
        List<SchemaSqlMapping> mappings = new ArrayList<>();

        Schema recordSchema = Schema.createRecord(name, null, nameSpace, false);

        List<Schema.Field> fields = new ArrayList<>();

        if (resultSet != null) {

            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();

            for (int x = 1; x <= columnCount; x++) {

                String columnName = resultSetMetaData.getColumnName(x);
                int sqlColumnType = resultSetMetaData.getColumnType(x);
                String schemaName = columnName.toLowerCase();

                Schema.Type schemaType = parseSqlSchemaToParquetSchema(sqlColumnType);
                mappings.add(new SchemaSqlMapping(schemaName, columnName, sqlColumnType, schemaType));
                fields.add(createNullableField(recordSchema, schemaName, schemaType));
            }
        }

        recordSchema.setFields(fields);
        schemaResults.setMappings(mappings);
        schemaResults.setParsedSchema(recordSchema);

        return schemaResults;
    }


    public Schema.Type parseSqlSchemaToParquetSchema(int sqlColumnType) {
        return switch (sqlColumnType) {
            case Types.BOOLEAN -> Schema.Type.BOOLEAN;
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER -> Schema.Type.INT;
            case Types.ROWID, Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR, Types.NCHAR, Types.NVARCHAR, Types.LONGNVARCHAR, Types.SQLXML -> Schema.Type.STRING;
            case Types.REAL -> Schema.Type.FLOAT;
            case Types.DOUBLE, Types.DECIMAL, Types.NUMERIC, Types.FLOAT -> Schema.Type.DOUBLE;
            case Types.DATE, Types.TIME, Types.TIMESTAMP, Types.TIME_WITH_TIMEZONE, Types.TIMESTAMP_WITH_TIMEZONE, Types.BIGINT -> Schema.Type.LONG;
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.NULL, Types.OTHER, Types.JAVA_OBJECT, Types.DISTINCT, Types.STRUCT, Types.ARRAY, Types.BLOB, Types.CLOB, Types.REF, Types.DATALINK, Types.NCLOB, Types.REF_CURSOR -> Schema.Type.BYTES;
            default -> null;
        };

    }


    public Schema.Field createNullableField(Schema recordSchema, String columnName, Schema.Type type) {

        Schema intSchema = Schema.create(type);
        Schema nullSchema = Schema.create(Schema.Type.NULL);

        List<Schema> fieldSchemas = new ArrayList<>();
        fieldSchemas.add(intSchema);
        fieldSchemas.add(nullSchema);

        Schema fieldSchema = recordSchema.createUnion(fieldSchemas);

        return new Schema.Field(columnName, fieldSchema, null, null);
    }

}
