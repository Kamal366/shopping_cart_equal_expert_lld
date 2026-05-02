package com.equalexperts.cart.model.response;

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
