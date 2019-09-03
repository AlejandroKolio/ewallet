package com.ashakhov.ewallet.utils;

import com.ashakhov.ewallet.exceptions.ApiClientException;
import com.ashakhov.ewallet.models.CurrencyCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @author Alexander Shakhov
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EWalletHandler {

    @NonNull
    public static Double convert(double amount, @NonNull CurrencyCode from, @NonNull CurrencyCode to) {
        final Double rate = rate(from, to);
        return (amount * rate);
    }

    @NonNull
    private static Double rate(@NonNull CurrencyCode source, @NonNull CurrencyCode target) {
        switch (source) {
            case EUR:
                switch (target) {
                    case USD:
                        return 1.11;
                    case RUB:
                        return 73.90;
                    default:
                        return 1.0;
                }
            case RUB:
                switch (target) {
                    case USD:
                        return 0.015;
                    case EUR:
                        return 0.0135;
                    default:
                        return 1.0;
                }
            case USD:
                switch (target) {
                    case EUR:
                        return 0.90;
                    case RUB:
                        return 65.72;
                    default:
                        return 1.0;
                }
            default:
                throw new ApiClientException("Unsupported currency type");
        }
    }
}
