package io.tsuihc.taskexecutor.task;

import io.tsuihc.taskexecutor.model.Task;

import java.util.List;

public interface TaskAccessController {

    void saveTask(Task task);

    List<Task> findExecutableTasks();

    int updateProcessing(Long taskId, Boolean processing, Long expectLastAccessTime, Long setLastAccessTime);

    void updateStatus(Long taskId, Integer status);

}
