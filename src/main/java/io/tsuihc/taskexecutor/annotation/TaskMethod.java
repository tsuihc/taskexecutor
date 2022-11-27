package io.tsuihc.taskexecutor.annotation;

import io.tsuihc.taskexecutor.model.TaskType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TaskMethod {

    TaskType type() default TaskType.NORMAL;

    Class<? extends Throwable>[] retryExceptions() default {};

}
