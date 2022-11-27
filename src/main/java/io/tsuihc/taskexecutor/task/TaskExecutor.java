package io.tsuihc.taskexecutor.task;

import io.tsuihc.taskexecutor.annotation.TaskMethod;
import io.tsuihc.taskexecutor.model.InvokeTarget;
import io.tsuihc.taskexecutor.model.Task;
import io.tsuihc.taskexecutor.model.TaskStatus;
import io.tsuihc.taskexecutor.model.TaskType;
import io.tsuihc.taskexecutor.utils.ReflectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class TaskExecutor {

    private final long monitorInterval;
    private final long executePoolSize;
    private final long timeoutInterval;
    private final TaskAccessController controller;
    private final TaskContainer container;

    private volatile boolean running;
    private Thread monitorThread;


    public void start() {
        running = true;
        monitorThread = new Thread(() -> {
            while (running) {
                sleepQuietly(monitorInterval);
                List<Task> executableTasks = controller.findExecutableTasks();
                executableTasks.forEach(this::tryLockAndExecute);
            }
        });
        monitorThread.start();
    }

    private void tryLockAndExecute(Task task) {
        boolean locked = false;
        try {
            locked = tryLock(task);
            if (!locked) {
                return;
            }
            execute(task);
        } catch (Exception e) {
            log.error("Error processing task[{}]", task.getId(), e);
        } finally {
            if (locked) {
                releaseLock(task);
            }
        }
    }

    private boolean tryLock(Task task) {
        boolean forceLock = false;
        // weather the lock is timeout or not
        if (task.isProcessing()) {
            boolean timeout = System.currentTimeMillis() - task.getLastAccessTime() > timeoutInterval;
            if (timeout) {
                log.info("task[{}] lock timeout, force lock it.", task.getId());
                forceLock = true;
            } else {
                log.info("task[{}] is being processed", task.getId());
                return false;
            }
        }
        boolean processing = true;
        long lockTime = System.currentTimeMillis();
        int affected = controller.updateProcessing(task.getId(), processing, task.getLastAccessTime(), lockTime);
        boolean locked = affected > 0;
        if (locked && forceLock) {
            controller.updateStatus(task.getId(), TaskStatus.FAILED.code());
            return false;
        }
        if (locked) {
            task.setProcessing(processing);
            task.setLastAccessTime(lockTime);
        }
        return locked;
    }

    private void execute(Task task) throws ClassNotFoundException {
        InvokeTarget invokeTarget = InvokeTarget.deserialize(task.getInvokeTarget());
        Class<?> invokeClass = Class.forName(invokeTarget.getClassName());
        Object bean = container.getBean(invokeClass);

        Method invokeMethod = ReflectionUtils.findMethod(invokeClass, invokeTarget.getMethodName(), invokeTarget.getArgTypes());
        if (invokeMethod == null) {
            throw new RuntimeException(String.format("Can't find invoke method[%s] of invoke class[%s]", invokeTarget.getMethodName(), invokeClass.getSimpleName()));
        }
        TaskMethod annotation = invokeMethod.getAnnotation(TaskMethod.class);
        if (annotation == null) {
            throw new RuntimeException(String.format("Can't find annotation[%s] on method[%s] of invoke class[%s]", TaskMethod.class.getSimpleName(), invokeMethod.getName(), invokeClass.getSimpleName()));
        }
        TaskType taskType = annotation.type();
        try {
            Object result = ReflectionUtils.invokeMethod(invokeMethod, bean, invokeTarget.getArgs());
            if (result == null && taskType == TaskType.LOOP) {
                return;
            }
            controller.updateStatus(task.getId(), TaskStatus.SUCCESS.code());
            if (result != null) {
                if (!(result instanceof InvokeTarget)) {
                    throw new RuntimeException(String.format("Can't deal the invoke result[%s] which is not [%s]", result.getClass().getName(), InvokeTarget.class.getName()));
                }
                Task nextTask = buildTask((InvokeTarget) result);
                controller.saveTask(nextTask);
            }
        } catch (Throwable ex) {
            boolean isRetryException = false;
            for (Class<? extends Throwable> retryException : annotation.retryExceptions()) {
                if (retryException.isAssignableFrom(ex.getClass())) {
                    isRetryException = true;
                    break;
                }
            }
            if (isRetryException) {
                log.info("Error process task[{}], but the exception is tolerated to retry", task.getId(), ex);
            } else {
                log.info("Error process task[{}]", task.getId(), ex);
                controller.updateStatus(task.getId(), TaskStatus.FAILED.code());
            }
        }
    }

    private Task buildTask(InvokeTarget invokeTarget) {
        Task task = new Task();
        task.setInvokeTarget(InvokeTarget.serialize(invokeTarget));
        task.setLastAccessTime(System.currentTimeMillis());
        task.setProcessing(false);
        task.setStatus(0);
        return task;
    }

    private void releaseLock(Task task) {
        int affected = controller.updateProcessing(task.getId(), false, task.getLastAccessTime(), System.currentTimeMillis());
        if (affected <= 0) {
            // release lock can be failed if the lock is timeout
            log.info("");
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ignored) {
        }
    }

    public void stop() {
        running = false;
        try {
            monitorThread.join();
        } catch (InterruptedException ignored) {
        }
    }

}
