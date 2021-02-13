package avro;

import avro.Schema.SchemaSqlMapping;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetTransformer {

    Object extract(SchemaSqlMapping mapping, ResultSet resultSet) throws SQLException;
}
