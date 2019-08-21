package com.ashakhov.ewallet.models;

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
public enum CurrencyCode {
    USD("usd"),
    EUR("eur"),
    RUB("rub");

    @NonNull
    private final String name;

    @NonNull
    public static CurrencyCode of(@NonNull String name) {
        return Arrays.stream(CurrencyCode.values())
                .filter(code -> code.name.equalsIgnoreCase(name))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException("There is no such Currency"));
    }
}
