package io.tsuihc.taskexecutor.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TaskStatus {
    RUNNING(0),
    SUCCESS(1),
    FAILED(2),
    TIMEOUT(3),
    ;

    private final int code;

    public int code() {
        return code;
    }
}
