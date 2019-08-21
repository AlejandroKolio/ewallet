package com.ashakhov.ewallet.exceptions.handler;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Alexander Shakhov
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class ErrorResponse {
    @NonNull
    private final Integer code;
    @NonNull
    private final String status;
    @NonNull
    private final String message;
}
