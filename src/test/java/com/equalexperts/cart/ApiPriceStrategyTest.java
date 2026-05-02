package com.equalexperts.cart;

import com.equalexperts.cart.common.PriceRetrievalException;
import com.equalexperts.cart.pricing.ApiPriceStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ApiPriceStrategyTest {

    private final ApiPriceStrategy priceStrategy = new ApiPriceStrategy();

    @ParameterizedTest
    @CsvSource({
            "cheerios, 8.43",
            "cornflakes, 2.52",
            "frosties, 4.99",
            "shreddies, 4.68",
            "weetabix, 9.98"
    })
    void shouldFetchCorrectPriceForAllProducts(String product, BigDecimal expectedPrice) {
        BigDecimal actualPrice = priceStrategy.getPrice(product);

        assertEquals(0, expectedPrice.compareTo(actualPrice),
                "Price mismatch for product: " + product);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ","invalid product"})
    void shouldThrowExceptionForInvalidOrBlankProductName(String productName) {
        assertThrows(PriceRetrievalException.class,
                () -> priceStrategy.getPrice(productName));
    }

    @Test
    void shouldThrowExceptionForNullProductName() {
        assertThrows(PriceRetrievalException.class,
                () -> priceStrategy.getPrice(null));
    }

    @Test
    void shouldNotReturnNullPriceForValidProduct() {
        BigDecimal price = priceStrategy.getPrice("cheerios");

        assertNotNull(price);
        assertTrue(price.compareTo(BigDecimal.ZERO) > 0);
    }


}
