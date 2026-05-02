package com.equalexperts.cart.tax;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DefaultTaxStrategy implements TaxStrategy{

    private static final BigDecimal TAX_RATE = new BigDecimal("0.125");


    @Override
    public BigDecimal calculate(BigDecimal subtotal) {
        return subtotal.multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
