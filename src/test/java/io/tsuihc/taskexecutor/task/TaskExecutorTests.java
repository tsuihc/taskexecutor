package io.tsuihc.taskexecutor.task;


import io.tsuihc.taskexecutor.annotation.TaskMethod;
import io.tsuihc.taskexecutor.model.InvokeTarget;
import io.tsuihc.taskexecutor.model.Task;
import io.tsuihc.taskexecutor.model.TaskStatus;
import io.tsuihc.taskexecutor.model.TaskType;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class TaskExecutorTests {

    @Test
    public void test() {
        String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/test";
        String username = "root";
        String password = "123456";

        JdbcTaskAccessController controller = null;
        MemTaskContainer container = null;
        TaskExecutor executor = null;
        try {
            controller = new JdbcTaskAccessController(jdbcUrl, username, password);
            controller.start();
            container = new MemTaskContainer();
            executor = new TaskExecutor(1000L, 10, 10 * 1000L, controller, container);
            executor.start();

            Task task = new Task();
            InvokeTarget invokeTarget = new InvokeTarget(TaskExecutorTests.class.getName(), "testExecute1", null, null);
            task.setInvokeTarget(InvokeTarget.serialize(invokeTarget));
            task.setLastAccessTime(System.currentTimeMillis());
            task.setProcessing(false);
            task.setStatus(TaskStatus.RUNNING.code());
            controller.saveTask(task);

            TimeUnit.MINUTES.sleep(3);
        } catch (Exception e) {
            e.printStackTrace();
            if (controller != null) {
                try {
                    controller.stop();
                } catch (Exception ignored) {
                }
            }
            if (executor != null) {
                executor.stop();
            }
        }
    }

    @TaskMethod
    public InvokeTarget testExecute1() {
        System.out.println("TestExecute1 executed..");
        return new InvokeTarget(TaskExecutorTests.class.getName(), "testExecute2", null, null);
    }

    private int called = 0;

    @TaskMethod(type = TaskType.LOOP)
    public InvokeTarget testExecute2() {
        System.out.println("TestExecuted2 executed called=" + ++called);
        if (called == 3) {
            return new InvokeTarget(TaskExecutorTests.class.getName(), "testExecute3", null, null);
        } else {
            return null;
        }
    }

    @TaskMethod(retryExceptions = {NullPointerException.class})
    public InvokeTarget testExecute3() {
        System.out.println("TestExecuted3 executed called=" + ++called);
        if (called < 5) {
            throw new NullPointerException();
        } else {
            return new InvokeTarget(TaskExecutorTests.class.getName(), "testExecute4", new Class[]{Integer.class}, new Object[]{1});
        }
    }

    @TaskMethod
    public InvokeTarget testExecute4(Integer arg1) {
        System.out.println("TestExecuted4 executed called=" + arg1);
        return new InvokeTarget(TaskExecutorTests.class.getName(), "testExecute5", null, null);
    }

    @TaskMethod
    public void testExecute5() {
        System.out.println("TestExecuted5 executed");
        try {
            Thread.sleep(15 * 1000L);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }


}
