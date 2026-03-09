package com.grocery.groceryapp.repository;

import com.grocery.groceryapp.entity.CartProduct;
import com.grocery.groceryapp.entity.CartProductKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, CartProductKey> {
}