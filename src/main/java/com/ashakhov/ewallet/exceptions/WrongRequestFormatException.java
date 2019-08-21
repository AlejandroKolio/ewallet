package com.ashakhov.ewallet.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author Alexander Shakhov
 */
@Getter
@AllArgsConstructor
public class WrongRequestFormatException extends AbstractFormattedException {
    private static final long serialVersionUID = 1L;
    @NonNull
    private final String message;
}
