package com.equalexperts.cart;

import com.equalexperts.cart.tax.DefaultTaxStrategy;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class DefaultTaxStrategyTest {

    private final DefaultTaxStrategy taxStrategy = new DefaultTaxStrategy();

    @Test
    void shouldCalculateTaxCorrectly_forSampleInput() {
        BigDecimal subtotal = new BigDecimal("15.02");
        BigDecimal tax = taxStrategy.calculate(subtotal);
        assertEquals(new BigDecimal("1.88"), tax);
    }

    @Test
    void shouldReturnZeroTax_whenSubtotalIsZero() {
        BigDecimal tax = taxStrategy.calculate(BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.00"), tax);
    }

    @Test
    void shouldRoundHalfUp_toTwoDecimalPlaces() {
        // 10 * 0.125 = 1.25 (no rounding needed)
        BigDecimal subtotal = new BigDecimal("10.00");
        BigDecimal tax = taxStrategy.calculate(subtotal);

        assertEquals(new BigDecimal("1.25"), tax);
    }

    @Test
    void shouldRoundCorrectly_forFractionalValues() {
        // 0.01 * 0.125 = 0.00125 → rounds to 0.00
        BigDecimal subtotal = new BigDecimal("0.01");
        BigDecimal tax = taxStrategy.calculate(subtotal);
        assertEquals(new BigDecimal("0.00"), tax);
    }

    @Test
    void shouldHandleLargeValuesCorrectly() {
        BigDecimal subtotal = new BigDecimal("9999.99");
        BigDecimal tax = taxStrategy.calculate(subtotal);
        assertEquals(new BigDecimal("1250.00"), tax);
    }

    @Test
    void shouldThrowException_whenSubtotalIsNull() {
        assertThrows(NullPointerException.class,
                () -> taxStrategy.calculate(null));
    }
}
