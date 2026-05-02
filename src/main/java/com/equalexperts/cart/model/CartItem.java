package com.equalexperts.cart.model;

import java.math.BigDecimal;

public class CartItem {

    private final Product product;
    private int quantity;


    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public BigDecimal totalPrice() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public String getName(){
        return product.getName();
    }
}
