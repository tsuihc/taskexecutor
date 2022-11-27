package io.tsuihc.taskexecutor.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class InvokeTargetTests {

    @Test
    public void testSerialize() throws ClassNotFoundException {
        String className = "io.tsuihc.taskexecutor.InvokeTargetTests";
        String methodName = "testSerialize";
        Class<?>[] argTypes = null;
        Object[] args = null;
        InvokeTarget invokeTarget = new InvokeTarget(className, methodName, argTypes, args);
        String serialize = InvokeTarget.serialize(invokeTarget);
        System.out.println(serialize);
        InvokeTarget deserialize = InvokeTarget.deserialize(serialize);
        for (Object arg : deserialize.getArgs()) {
            System.out.println(arg.getClass().getName());
        }
    }


}
