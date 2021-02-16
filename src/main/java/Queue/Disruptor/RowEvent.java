package Queue.Disruptor;

public class RowEvent<T> {
    private T row;

    public RowEvent(){}
    public RowEvent(T row) {
        this.row = row;
    }

    public T getRow() {
        return row;
    }

    public void setRow(T row) {
        this.row = row;
    }
}