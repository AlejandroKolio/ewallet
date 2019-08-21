package com.ashakhov.ewallet.utils;

import com.ashakhov.ewallet.exceptions.ApiClientException;
import com.ashakhov.ewallet.models.CurrencyCode;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @author Alexander Shakhov
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EWalletHandler {

    public static String getSqlScript(@NonNull String pathToFile) {
        Stream<String> lines = null;
        try {
            final Path path = Paths.get(
                    Objects.requireNonNull(EWalletHandler.class.getClassLoader().getResource(pathToFile)).toURI());
            lines = Files.lines(path);
            return lines.collect(Collectors.joining("\n"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        } finally {
            Objects.requireNonNull(lines).close();
        }
        throw new ApiClientException("Error while reading sql scripts");
    }

    public static Double convert(double amount, @NonNull CurrencyCode from, @NonNull CurrencyCode to) {
        final Double rate = rate(from, to);
        return (amount * rate);
    }

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
                        return 66.74;
                    default:
                        return 1.0;
                }
            default:
                throw new ApiClientException("Unsupported currency type");
        }
    }
}
