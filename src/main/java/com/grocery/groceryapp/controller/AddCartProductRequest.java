package com.grocery.groceryapp.controller;

public class AddCartProductRequest {

    private Long cartId;
    private Long productId;
    private int quantity;

    public Long getCartId() { return cartId; }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}