package com.equalexperts.cart.tax;

import java.math.BigDecimal;

public interface TaxStrategy {

    BigDecimal calculate(BigDecimal subtotal);
}
