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

                Schema.Type schemaType = parseSchemaType(sqlColumnType);
                mappings.add(new SchemaSqlMapping(schemaName, columnName, sqlColumnType, schemaType));
                fields.add(createNullableField(recordSchema, schemaName, schemaType));
            }
        }

        recordSchema.setFields(fields);

        schemaResults.setMappings(mappings);
        schemaResults.setParsedSchema(recordSchema);

        return schemaResults;
    }


    public Schema.Type parseSchemaType(int sqlColumnType) {
        switch (sqlColumnType) {

            case Types.BOOLEAN:
                return Schema.Type.BOOLEAN;

            case Types.TINYINT:             // 1 byte
            case Types.SMALLINT:            // 2 bytes
            case Types.INTEGER:             // 4 bytes
                return Schema.Type.INT;     // 32 bit (4 bytes) (signed)

            case Types.ROWID:
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.SQLXML:
                return Schema.Type.STRING;  // unicode string

            case Types.REAL:
                return Schema.Type.FLOAT;

            case Types.DOUBLE:
            case Types.DECIMAL:             // Exact numerical (5 - 17 bytes)
            case Types.NUMERIC:             // Exact numerical (5 - 17 bytes)
            case Types.FLOAT:
                return Schema.Type.DOUBLE;  // A 64-bit IEEE double-float

            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.TIME_WITH_TIMEZONE:
            case Types.TIMESTAMP_WITH_TIMEZONE:
            case Types.BIGINT:              // 8 bytes
                return Schema.Type.LONG;    // 64 bit (signed)

            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.NULL:
            case Types.OTHER:
            case Types.JAVA_OBJECT:
            case Types.DISTINCT:
            case Types.STRUCT:
            case Types.ARRAY:
            case Types.BLOB:
            case Types.CLOB:
            case Types.REF:
            case Types.DATALINK:
            case Types.NCLOB:
            case Types.REF_CURSOR:
                return Schema.Type.BYTES;
        }

        return null;
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
