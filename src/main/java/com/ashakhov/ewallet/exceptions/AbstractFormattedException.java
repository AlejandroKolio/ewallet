package com.ashakhov.ewallet.exceptions;

import lombok.NonNull;

/**
 * Base form of runtime exception, adds convenient formatting.
 * @author Alexander Shakhov
 */
public abstract class AbstractFormattedException extends Throwable {
    private static final long serialVersionUID = 1L;

    public AbstractFormattedException() {
        super();
    }

    public AbstractFormattedException(@NonNull String message, Object... params) {
        super(String.format(message, params));
    }

    public AbstractFormattedException(@NonNull Throwable cause, @NonNull String message, Object... params) {
        super(String.format(message, params), cause);
    }

    public AbstractFormattedException(@NonNull Throwable cause) {
        super(cause);
    }
}
