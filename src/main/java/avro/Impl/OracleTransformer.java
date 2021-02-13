package avro.Impl;

import avro.ResultSetTransformer;
import avro.Schema.SchemaSqlMapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class OracleTransformer implements ResultSetTransformer {

    @Override
    public Object extract(SchemaSqlMapping mapping, ResultSet resultSet) throws SQLException {

        return switch (mapping.getSqlType()) {
            case Types.BOOLEAN -> resultSet.getBoolean(mapping.getSqlColumnName());
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT, Types.ROWID -> resultSet.getInt(mapping.getSqlColumnName());
            case Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR, Types.NCHAR, Types.NVARCHAR, Types.LONGNVARCHAR, Types.SQLXML -> resultSet.getString(mapping.getSqlColumnName());
            case Types.REAL, Types.FLOAT -> resultSet.getFloat(mapping.getSqlColumnName());
            case Types.DOUBLE -> resultSet.getDouble(mapping.getSqlColumnName());
            case Types.NUMERIC -> resultSet.getBigDecimal(mapping.getSqlColumnName());
            case Types.DECIMAL -> resultSet.getBigDecimal(mapping.getSqlColumnName());
            case Types.DATE -> resultSet.getDate(mapping.getSqlColumnName()).getTime();
            case Types.TIME, Types.TIME_WITH_TIMEZONE -> resultSet.getTime(mapping.getSqlColumnName()).getTime();
            case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> resultSet.getTimestamp(mapping.getSqlColumnName()).getTime();
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.NULL, Types.OTHER, Types.JAVA_OBJECT, Types.DISTINCT, Types.STRUCT, Types.ARRAY, Types.BLOB, Types.CLOB, Types.REF, Types.DATALINK, Types.NCLOB, Types.REF_CURSOR -> resultSet.getByte(mapping.getSqlColumnName());
            default -> resultSet.getString(mapping.getSqlColumnName());
        };
    }
}
