package com.equalexperts.cart;

import com.equalexperts.cart.common.PriceRetrievalException;
import com.equalexperts.cart.pricing.PriceStrategy;
import com.equalexperts.cart.service.ShoppingCart;
import com.equalexperts.cart.tax.TaxStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingCartMockitoTest {

    @Mock
    private PriceStrategy priceStrategy;

    @Mock
    private TaxStrategy taxStrategy;

    @Test
    void shouldCalculateTotalsUsingMockedDependencies() {
        when(priceStrategy.getPrice("cornflakes")).thenReturn(new BigDecimal("2.52"));
        when(taxStrategy.calculate(new BigDecimal("5.04"))).thenReturn(new BigDecimal("0.63"));

        ShoppingCart cart = new ShoppingCart(priceStrategy, taxStrategy);

        cart.addProduct("cornflakes", 2);

        assertEquals(new BigDecimal("5.04"), cart.getSubtotal());
        assertEquals(new BigDecimal("0.63"), cart.getTax());
        assertEquals(new BigDecimal("5.67"), cart.getTotal());
        verify(priceStrategy).getPrice("cornflakes");
        verify(taxStrategy, times(2)).calculate(new BigDecimal("5.04"));
    }

    @Test
    void shouldAggregateQuantityForSameProductUsingMockedPrice() {
        when(priceStrategy.getPrice("cornflakes")).thenReturn(new BigDecimal("2.52"));

        ShoppingCart cart = new ShoppingCart(priceStrategy, taxStrategy);

        cart.addProduct("cornflakes", 1);
        cart.addProduct("cornflakes", 2);

        assertEquals(3, cart.getItems().get("cornflakes").getQuantity());
        assertEquals(new BigDecimal("7.56"), cart.getSubtotal());
        verify(priceStrategy, times(2)).getPrice("cornflakes");
    }

    @Test
    void shouldNotCallPriceStrategyWhenProductNameIsInvalid() {
        ShoppingCart cart = new ShoppingCart(priceStrategy, taxStrategy);

        assertThrows(IllegalArgumentException.class,
                () -> cart.addProduct(" ", 1));

        verifyNoInteractions(priceStrategy, taxStrategy);
    }

    @Test
    void shouldPropagatePriceRetrievalExceptionFromPriceStrategy() {
        when(priceStrategy.getPrice("unknown"))
                .thenThrow(new PriceRetrievalException("Failed to fetch price"));

        ShoppingCart cart = new ShoppingCart(priceStrategy, taxStrategy);

        assertThrows(PriceRetrievalException.class,
                () -> cart.addProduct("unknown", 1));

        verify(priceStrategy).getPrice("unknown");
        verifyNoInteractions(taxStrategy);
    }
}
