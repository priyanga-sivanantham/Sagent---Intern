package com.grocery.groceryapp.repository;

import com.grocery.groceryapp.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    // ✅ Find user using email only
    Optional<Customer> findByEmail(String email);

}