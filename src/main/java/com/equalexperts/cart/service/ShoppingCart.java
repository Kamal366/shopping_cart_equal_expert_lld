package com.equalexperts.cart.service;

import com.equalexperts.cart.model.CartItem;
import com.equalexperts.cart.model.Product;
import com.equalexperts.cart.pricing.PriceStrategy;
import com.equalexperts.cart.tax.TaxStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ShoppingCart {

    private final PriceStrategy priceStrategy;
    private final TaxStrategy taxStrategy;

    private final Map<String, CartItem> items = new HashMap<>();
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    public ShoppingCart(PriceStrategy priceStrategy, TaxStrategy taxStrategy) {
        this.priceStrategy = priceStrategy;
        this.taxStrategy = taxStrategy;
    }

    public void addProduct(String name, int quantity) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Invalid product name");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        BigDecimal price = priceStrategy.getPrice(name);
        Product product = new Product(name, price);

        CartItem item = items.get(name);

        if (item == null) {
            items.put(name, new CartItem(product, quantity));
        }else {
            item.addQuantity(quantity);
        }
    }

    public BigDecimal getSubtotal() {
        return items.values().stream()
                .map(CartItem::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void applyDiscount(BigDecimal discountPercentage) {
        if (discountPercentage == null
                || discountPercentage.compareTo(BigDecimal.ZERO) < 0
                || discountPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        }

        this.discountPercentage = discountPercentage;
    }

    public BigDecimal getDiscount() {
        return getSubtotal()
                .multiply(discountPercentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTax() {
        return taxStrategy.calculate(getSubtotalAfterDiscount());
    }

    public BigDecimal getTotal() {
        return getSubtotalAfterDiscount()
                .add(getTax())
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Map<String, CartItem> getItems() {
        return Collections.unmodifiableMap(items);
    }

    private BigDecimal getSubtotalAfterDiscount() {
        return getSubtotal()
                .subtract(getDiscount())
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void removeProduct(String product) {

        if (product == null || product.isBlank()) {
            throw new IllegalArgumentException("Invalid product name");
        }

        if(items.get(product) != null) {
            items.remove(product);
        }
    }
}
