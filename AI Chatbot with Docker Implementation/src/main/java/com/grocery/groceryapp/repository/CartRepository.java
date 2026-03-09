package com.grocery.groceryapp.repository;

import com.grocery.groceryapp.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCustomerCustomerId(Long customerId);

}