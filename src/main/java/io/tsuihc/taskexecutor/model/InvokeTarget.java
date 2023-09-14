package io.tsuihc.taskexecutor.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class InvokeTarget {
  private String className;
  private String methodName;
  private Class<?>[] argTypes;
  private Object[] args;

  private static final String DELIMITER = ";";

  private static final Gson gson = new Gson();

  public static String serialize(InvokeTarget target) {
    StringBuilder builder = new StringBuilder();
    builder.append(target.className)
           .append(DELIMITER)
           .append(target.methodName)
           .append(DELIMITER);
    if (target.argTypes != null) {
      if (target.args == null || target.args.length != target.argTypes.length) {
        throw new RuntimeException(String.format("The size of args[%s] doesn't match the size of arg types[%d].", target.args == null ? "null" : target.args.length, target.argTypes.length));
      }
      List<String> argTypeClasses = new ArrayList<>();
      for (Class<?> argType : target.argTypes) {
        argTypeClasses.add(argType.getName());
      }
      builder.append(gson.toJson(argTypeClasses));
      builder.append(DELIMITER);
      List<String> argJsons = new ArrayList<>();
      for (int i = 0; i < target.args.length; i++) {
        argJsons.add(gson.toJson(target.args[i], target.argTypes[i]));
      }
      builder.append(gson.toJson(argJsons));
    }
    return builder.toString();
  }

  public static InvokeTarget deserialize(String str) throws ClassNotFoundException {
    String className;
    String methodName;
    Class<?>[] argTypes;
    Object[] args;

    int start = 0;
    int index = StringUtils.indexOf(str, DELIMITER, start);
    className = StringUtils.substring(str, start, index);
    start = index + 1;
    index = StringUtils.indexOf(str, DELIMITER, start);
    methodName = StringUtils.substring(str, start, index);
    start = index + 1;
    index = StringUtils.indexOf(str, DELIMITER, start);
    if (index == -1) {
      return new InvokeTarget(className, methodName, null, null);
    }
    List<String> argTypeClasses = gson.fromJson(StringUtils.substring(str, start, index), new TypeToken<List<String>>() {
    }.getType());
    argTypes = new Class<?>[argTypeClasses.size()];
    for (int i = 0; i < argTypes.length; i++) {
      argTypes[i] = Class.forName(argTypeClasses.get(i));
    }
    start = index + 1;
    List<String> argJsons = gson.fromJson(StringUtils.substring(str, start, str.length()), new TypeToken<List<String>>() {
    }.getType());
    args = new Object[argJsons.size()];
    for (int i = 0; i < args.length; i++) {
      args[i] = gson.fromJson(argJsons.get(i), argTypes[i]);
    }
    return new InvokeTarget(className, methodName, argTypes, args);
  }

}
