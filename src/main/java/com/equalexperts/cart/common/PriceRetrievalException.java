package com.equalexperts.cart.common;

public class PriceRetrievalException extends RuntimeException {
    public PriceRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
    public PriceRetrievalException(String message) {
        super(message);
    }
}
