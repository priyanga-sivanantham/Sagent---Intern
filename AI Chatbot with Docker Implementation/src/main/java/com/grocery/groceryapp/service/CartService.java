package com.grocery.groceryapp.service;

import com.grocery.groceryapp.entity.Cart;
import com.grocery.groceryapp.entity.Customer;
import com.grocery.groceryapp.repository.CartRepository;
import com.grocery.groceryapp.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CustomerRepository customerRepository;


    // Create cart for customer (only one cart)
    public Cart createCart(Long customerId) {

        Optional<Cart> existingCart =
                cartRepository.findByCustomerCustomerId(customerId);

        if(existingCart.isPresent()) {

            return existingCart.get();

        }

        Customer customer =
                customerRepository.findById(customerId).orElseThrow();

        Cart cart = new Cart();

        cart.setCustomer(customer);

        cart.setTotalAmount(0.0);

        cart.setTotalItems(0);

        cart.setDiscount(0.0);

        return cartRepository.save(cart);

    }


    public Cart getCartByCustomer(Long customerId) {

        return cartRepository
                .findByCustomerCustomerId(customerId)
                .orElseThrow();

    }


    public List<Cart> getAllCarts(){

        return cartRepository.findAll();

    }


    public void deleteCart(Long cartId){

        cartRepository.deleteById(cartId);

    }

}
