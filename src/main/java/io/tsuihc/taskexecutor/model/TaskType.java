package io.tsuihc.taskexecutor.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TaskType {

  NORMAL(0),
  /**
   * A loop task may not advance the task chain
   */
  LOOP(1),
  ;

  private final int code;

  public int code() {
    return code;
  }
}
