package avro;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public interface ResultSetTransformer {

    Object extract(SchemaSqlMapping mapping, ResultSet resultSet) throws SQLException;
}
