package com.grocery.groceryapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig {

    // ✅ PASSWORD ENCODER
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth

                        // allow auth APIs
                        .requestMatchers("/customers/**").permitAll()

                        // allow carts
                        .requestMatchers("/products/**").permitAll()
                        .requestMatchers("/carts/**").permitAll()

                        // allow products
                        .requestMatchers("/orders/**").permitAll()

                        // allow everything (dev mode)
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}