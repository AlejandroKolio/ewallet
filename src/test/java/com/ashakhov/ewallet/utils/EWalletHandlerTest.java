package com.ashakhov.ewallet.utils;

import static com.ashakhov.ewallet.models.CurrencyCode.EUR;
import static com.ashakhov.ewallet.models.CurrencyCode.RUB;
import static com.ashakhov.ewallet.models.CurrencyCode.USD;
import static org.assertj.core.api.Assertions.assertThat;

import com.ashakhov.ewallet.models.CurrencyCode;
import java.util.stream.Stream;
import lombok.NonNull;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Alexander Shakhov
 */
public class EWalletHandlerTest {

    @ParameterizedTest()
    @MethodSource("testData")
    public void currencyConverterTest(double amount, @NonNull CurrencyCode from, @NonNull CurrencyCode to, double expected) {
        final Double actual = EWalletHandler.convert(amount, from, to);
        assertThat(expected).isCloseTo(actual, Percentage.withPercentage(1));
    }

    private static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of(20.0, USD, RUB, 1314.35),
                Arguments.of(20.0, USD, EUR, 18.02),
                Arguments.of(20.0, USD, USD, 20.0),
                Arguments.of(20.0, EUR, RUB, 1476.73),
                Arguments.of(20.0, EUR, EUR, 20.0),
                Arguments.of(20.0, EUR, USD, 22.20),
                Arguments.of(20.0, RUB, RUB, 20.0),
                Arguments.of(20.0, RUB, EUR, 0.27),
                Arguments.of(20.0, RUB, USD, 0.30));
    }
}
