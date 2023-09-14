package io.tsuihc.taskexecutor.model;

import lombok.Data;

@Data
public class Task {

  /**
   * The identity of each task
   */
  private Long id;

  /**
   * Path of invoke method: ClassName;MethodNam;ArgTypes
   */
  private String invokeTarget;

  /**
   * The variable to make optimistic concurrency control
   */
  private Long lastAccessTime;

  /**
   * The variable to mark whether the task is held or not
   */
  private Boolean processing;

  /**
   * Task status represented by integer
   */
  private Integer status;

  public boolean isProcessing() {
    return Boolean.TRUE.equals(processing);
  }

}
