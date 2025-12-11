package io.notfound.counsel_back.common.response;

import lombok.Getter;

@Getter
public class ResponseMessage<T> {
    private int status;
    private String message;
    private T data;

    private ResponseMessage(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseMessage<T> of(int status, String message, T data) {
        return new ResponseMessage<>(status, message, data);
    }

    public static <T> ResponseMessage<T> of(int status, String message) {
        return new ResponseMessage<>(status, message, null);
    }
}
