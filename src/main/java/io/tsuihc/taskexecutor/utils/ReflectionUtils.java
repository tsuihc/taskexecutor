package io.tsuihc.taskexecutor.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;

public class ReflectionUtils {

    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        for (Class<?> searchType = clazz; searchType != null; searchType = searchType.getSuperclass()) {
            Method[] methods = searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods();
            for (Method method : methods) {
                if (name.equals(method.getName()) && (paramTypes == null || hasSameParams(method, paramTypes))) {
                    return method;
                }
            }
        }
        return null;
    }

    public static Object invokeMethod(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (Exception e) {
            handleReflectionException(e);
            throw new IllegalStateException("Should never get here");
        }
    }

    public static void handleReflectionException(Exception ex) {
        if (ex instanceof NoSuchMethodException) {
            throw new IllegalStateException("Method not found: " + ex.getMessage());
        } else if (ex instanceof IllegalAccessException) {
            throw new IllegalStateException("Could not access method or field: " + ex.getMessage());
        } else {
            if (ex instanceof InvocationTargetException) {
                handleInvocationTargetException((InvocationTargetException) ex);
            }

            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                throw new UndeclaredThrowableException(ex);
            }
        }
    }

    public static void handleInvocationTargetException(InvocationTargetException ex) {
        rethrowRuntimeException(ex.getTargetException());
    }

    public static void rethrowRuntimeException(Throwable ex) {
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        } else if (ex instanceof Error) {
            throw (Error) ex;
        } else {
            throw new UndeclaredThrowableException(ex);
        }
    }


    private static boolean hasSameParams(Method method, Class<?>[] paramTypes) {
        return paramTypes.length == method.getParameterCount() && Arrays.equals(paramTypes, method.getParameterTypes());
    }

}
