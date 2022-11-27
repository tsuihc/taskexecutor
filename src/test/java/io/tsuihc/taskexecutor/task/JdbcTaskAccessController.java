package io.tsuihc.taskexecutor.task;

import io.tsuihc.taskexecutor.model.Task;
import lombok.RequiredArgsConstructor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class JdbcTaskAccessController implements TaskAccessController {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    private Connection connection;

    public void start() throws Exception {
        connection = DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    public void saveTask(Task task) {
        try (
                PreparedStatement statement = connection.prepareStatement("insert into task(invoke_target, last_access_time, processing, status) values(?, ?, ?, ?) ")
        ) {
            statement.setString(1, task.getInvokeTarget());
            statement.setLong(2, task.getLastAccessTime());
            statement.setBoolean(3, task.getProcessing());
            statement.setInt(4, task.getStatus());
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Task> findExecutableTasks() {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select * from task where processing = false and status = 0")
        ) {
            List<Task> tasks = new ArrayList<>();
            while (resultSet.next()) {
                Task task = new Task();
                task.setId(resultSet.getLong(1));
                task.setInvokeTarget(resultSet.getString(2));
                task.setLastAccessTime(resultSet.getLong(3));
                task.setProcessing(resultSet.getBoolean(4));
                task.setStatus(resultSet.getInt(5));
                tasks.add(task);
            }
            return tasks;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int updateProcessing(Long taskId, Boolean processing, Long expectLastAccessTime, Long setLastAccessTime) {
        try (
                PreparedStatement statement = connection.prepareStatement("update task set processing = ?, last_access_time = ? where id = ? and last_access_time = ?")
        ) {
            statement.setBoolean(1, processing);
            statement.setLong(2, setLastAccessTime);
            statement.setLong(3, taskId);
            statement.setLong(4, expectLastAccessTime);
            return statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateStatus(Long taskId, Integer status) {
        try (
                PreparedStatement statement = connection.prepareStatement("update task set status = ? where id = ?")
        ) {
            statement.setInt(1, status);
            statement.setLong(2, taskId);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

}
