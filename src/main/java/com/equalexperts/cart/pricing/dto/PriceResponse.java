package com.equalexperts.cart.pricing.dto;

import java.math.BigDecimal;

public class PriceResponse {
    private String title;
    private BigDecimal price;

    public String getTitle() {
        return title;
    }
    public BigDecimal getPrice() {
        return price;
    }
}
