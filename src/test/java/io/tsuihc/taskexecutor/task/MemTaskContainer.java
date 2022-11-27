package io.tsuihc.taskexecutor.task;

import java.util.HashMap;
import java.util.Map;

public class MemTaskContainer implements TaskContainer {

    private static final Map<Class<?>, Object> beans = new HashMap<>();

    static {
        beans.put(TaskExecutorTests.class, new TaskExecutorTests());
    }

    @Override
    public Object getBean(Class<?> clazz) {
        return beans.get(clazz);
    }
}
