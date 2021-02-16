package Queue.BlockingQueue.Impl;

import Queue.BlockingQueue.QueueManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CSVQueueManager extends QueueManager<String[]> {

    @Override
    public void addAllFetchToQueue(ResultSet resultSet) throws SQLException {
        List<String[]> list = new ArrayList<>();
        while (resultSet.next()) {
            final int n = resultSet.getMetaData().getColumnCount();
            final String[] line = new String[n];
            for (int i = 1; i<=n; i++) {
                line[i-1] = resultSet.getString(i);
            }

            list.add(line);
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
