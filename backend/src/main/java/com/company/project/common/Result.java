package com.company.project.common;

import java.time.LocalDateTime;

/**
 * 统一响应结构：{ code, message, data, timestamp }，成功 code=0。
 *
 * @param <T> 业务数据类型
 */
public class Result<T> {

    private final int code;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "success", data);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
