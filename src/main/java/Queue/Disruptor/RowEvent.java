package Queue.Disruptor;

public class RowEvent<T> {
    private T resultSet;

    public RowEvent(){}
    public RowEvent(T resultSet) {
        this.resultSet = resultSet;
    }

    public T getResultSet() {
        return resultSet;
    }

    public void setResultSet(T resultSet) {
        this.resultSet = resultSet;
    }
}