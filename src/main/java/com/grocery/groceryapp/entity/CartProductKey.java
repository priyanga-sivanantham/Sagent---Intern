package com.grocery.groceryapp.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CartProductKey implements Serializable {

    @Column(name = "cart_id")
    private Long cartId;

    @Column(name = "product_id")
    private Long productId;

    public CartProductKey() {}

    public CartProductKey(Long cartId, Long productId) {
        this.cartId = cartId;
        this.productId = productId;
    }

    public Long getCartId() {
        return cartId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }


    // IMPORTANT

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;

        if (!(o instanceof CartProductKey)) return false;

        CartProductKey that = (CartProductKey) o;

        return Objects.equals(cartId, that.cartId)
                && Objects.equals(productId, that.productId);

    }


    @Override
    public int hashCode() {

        return Objects.hash(cartId, productId);

    }

}