package com.grocery.groceryapp.service;

import com.grocery.groceryapp.entity.*;
import com.grocery.groceryapp.repository.CartProductRepository;
import com.grocery.groceryapp.repository.CartRepository;
import com.grocery.groceryapp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartProductService {

    @Autowired
    private CartProductRepository cartProductRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;


    @Transactional
    public CartProduct addProduct(Long cartId, Long productId, int quantity) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));


        CartProductKey key = new CartProductKey(cartId, productId);


        // CHECK if already exists
        CartProduct existing = cartProductRepository.findById(key).orElse(null);

        if(existing != null) {

            existing.setQuantity(existing.getQuantity() + quantity);

            existing.setPrice(existing.getProduct().getProductPrice() * existing.getQuantity());

            return cartProductRepository.save(existing);

        }


        CartProduct cartProduct = new CartProduct();

        cartProduct.setId(key);

        cartProduct.setCart(cart);

        cartProduct.setProduct(product);

        cartProduct.setQuantity(quantity);

        cartProduct.setPrice(product.getProductPrice() * quantity);


        return cartProductRepository.save(cartProduct);

    }

}