package com.equalexperts.cart.pricing;

import java.math.BigDecimal;

public interface PriceStrategy {

    BigDecimal getPrice(String productName);
}
