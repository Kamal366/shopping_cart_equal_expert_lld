package com.equalexperts.cart;

import com.equalexperts.cart.pricing.PriceStrategy;
import com.equalexperts.cart.service.ShoppingCart;
import com.equalexperts.cart.tax.DefaultTaxStrategy;
import com.equalexperts.cart.tax.TaxStrategy;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartTest {

    private final PriceStrategy mockPrice = product -> switch (product) {
        case "cornflakes" -> new BigDecimal("2.52");
        case "weetabix" -> new BigDecimal("9.98");
        default -> BigDecimal.ZERO;
    };

    private final TaxStrategy taxStrategy = new DefaultTaxStrategy();

    @Test
    void shouldCalculateCartCorrectly() {
        ShoppingCart cart = new ShoppingCart(mockPrice, taxStrategy);

        cart.addProduct("cornflakes", 1);
        cart.addProduct("cornflakes", 1);
        cart.addProduct("weetabix", 1);

        assertEquals(new BigDecimal("15.02"), cart.getSubtotal());
        assertEquals(new BigDecimal("1.88"), cart.getTax());
        assertEquals(new BigDecimal("16.90"), cart.getTotal());
    }

    @Test
    void shouldAggregateSameProduct() {
        ShoppingCart cart = new ShoppingCart(mockPrice, taxStrategy);

        cart.addProduct("cornflakes", 1);
        cart.addProduct("cornflakes", 2);

        assertEquals(3, cart.getItems().get("cornflakes").getQuantity());
    }

    @Test
    void shouldThrowForInvalidQuantity() {
        ShoppingCart cart = new ShoppingCart(mockPrice, taxStrategy);

        assertThrows(IllegalArgumentException.class,
                () -> cart.addProduct("cornflakes", 0));
    }

    @Test
    void shouldThrowForNullProductName() {
        ShoppingCart cart = new ShoppingCart(mockPrice, taxStrategy);

        assertThrows(IllegalArgumentException.class,
                () -> cart.addProduct(null, 1));
    }

    @Test
    void shouldThrowForBlankProductName() {
        ShoppingCart cart = new ShoppingCart(mockPrice, taxStrategy);

        assertThrows(IllegalArgumentException.class,
                () -> cart.addProduct(" ", 1));
    }

    @Test
    void shouldThrowForNegativeQuantity() {
        ShoppingCart cart = new ShoppingCart(mockPrice, taxStrategy);

        assertThrows(IllegalArgumentException.class,
                () -> cart.addProduct("cornflakes", -1));
    }

    @Test
    void shouldReturnZeroTotalsForEmptyCart() {
        ShoppingCart cart = new ShoppingCart(mockPrice, taxStrategy);

        assertEquals(new BigDecimal("0.00"), cart.getSubtotal());
        assertEquals(new BigDecimal("0.00"), cart.getTax());
        assertEquals(new BigDecimal("0.00"), cart.getTotal());
    }

    @Test
    void shouldReturnUnmodifiableItemsMap() {
        ShoppingCart cart = new ShoppingCart(mockPrice, taxStrategy);

        cart.addProduct("cornflakes", 1);

        assertThrows(UnsupportedOperationException.class, () -> {
            cart.getItems().put("test", null);
        });
    }
}
