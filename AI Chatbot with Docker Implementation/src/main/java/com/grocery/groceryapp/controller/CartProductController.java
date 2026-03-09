package com.grocery.groceryapp.controller;

import com.grocery.groceryapp.entity.CartProduct;
import com.grocery.groceryapp.service.CartProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
@RestController
@RequestMapping("/cart-product")
@CrossOrigin(origins = "*")
public class CartProductController {

    @Autowired
    private CartProductService cartProductService;


    @PostMapping("/add")
    public ResponseEntity<?> addProduct(

            @RequestParam Long cartId,
            @RequestParam Long productId,
            @RequestParam int quantity
    ) {

        return ResponseEntity.ok(

                cartProductService.addProduct(cartId, productId, quantity)

        );

    }

}