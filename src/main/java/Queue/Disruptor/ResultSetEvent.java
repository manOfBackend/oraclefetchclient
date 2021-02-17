package Queue.Disruptor;
import java.sql.ResultSet;
public class ResultSetEvent {
    private ResultSet resultSet;

    public ResultSetEvent(){}
    public ResultSetEvent(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }
}