package com.grocery.groceryapp.controller;

import com.grocery.groceryapp.entity.Cart;
import com.grocery.groceryapp.service.CartService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carts")

public class CartController {


    @Autowired

    private CartService cartService;



    // Create Cart

    @PostMapping("/create/{customerId}")

    public ResponseEntity<Cart> createCart(

            @PathVariable Long customerId){

        return ResponseEntity.ok(

                cartService.createCart(customerId)

        );

    }



    // Get Customer Cart

    @GetMapping("/customer/{customerId}")

    public ResponseEntity<Cart> getCart(

            @PathVariable Long customerId){

        return ResponseEntity.ok(

                cartService.getCartByCustomer(customerId)

        );

    }



    // Get All carts

    @GetMapping

    public ResponseEntity<List<Cart>> getAll(){

        return ResponseEntity.ok(

                cartService.getAllCarts()

        );

    }



    // Delete cart

    @DeleteMapping("/{cartId}")

    public ResponseEntity<String> delete(

            @PathVariable Long cartId){

        cartService.deleteCart(cartId);

        return ResponseEntity.ok("Deleted");

    }

}