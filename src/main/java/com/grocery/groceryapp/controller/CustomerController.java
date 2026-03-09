package com.grocery.groceryapp.controller;

import com.grocery.groceryapp.entity.Customer;
import com.grocery.groceryapp.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.grocery.groceryapp.dto.LoginRequest;
import java.util.Optional;

@RestController
@RequestMapping("/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ✅ REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@RequestBody Customer customer) {

        Optional<Customer> existing =
                customerRepository.findByEmail(customer.getEmail());

        if (existing.isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Email already registered");
        }

        customer.setPassword(
                passwordEncoder.encode(customer.getPassword())
        );

        Customer saved = customerRepository.save(customer);

        return ResponseEntity.ok(saved);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Optional<Customer> optionalCustomer =
                customerRepository.findByEmail(request.getEmail());

        if (optionalCustomer.isEmpty()) {
            return ResponseEntity.status(401)
                    .body("Email not registered");
        }

        Customer customer = optionalCustomer.get();

        if (!passwordEncoder.matches(
                request.getPassword(),
                customer.getPassword())) {

            return ResponseEntity.status(401)
                    .body("Password is incorrect");
        }

        return ResponseEntity.ok(customer);
    }
}