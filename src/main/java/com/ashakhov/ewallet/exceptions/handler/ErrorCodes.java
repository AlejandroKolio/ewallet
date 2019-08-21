package com.ashakhov.ewallet.exceptions.handler;

import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.Arrays;
import java.util.NoSuchElementException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author Alexander Shakhov
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCodes {
    BAD_REQUEST(400, HttpResponseStatus.BAD_REQUEST, "Bad Format"),
    NOT_FOUND(404, HttpResponseStatus.NOT_FOUND, "Unknown Resource"),
    SERVER_ERROR(500, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Api Client Error");

    private final int code;
    @NonNull
    private final HttpResponseStatus status;
    @NonNull
    private final String message;

    @NonNull
    public static ErrorCodes of(@NonNull HttpResponseStatus status) {
        return Arrays.stream(ErrorCodes.values())
                .filter(error -> error.status.equals(status))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException("There is no such Error status"));
    }

    @NonNull
    public static ErrorCodes of(int code) {
        return Arrays.stream(ErrorCodes.values())
                .filter(error -> error.code == code)
                .findAny()
                .orElseThrow(() -> new NoSuchElementException("There is no such Error code"));
    }

    @NonNull
    public static ErrorCodes of(@NonNull String message) {
        return Arrays.stream(ErrorCodes.values())
                .filter(error -> error.message.equalsIgnoreCase(message))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException("There is no such Error message"));
    }
}
