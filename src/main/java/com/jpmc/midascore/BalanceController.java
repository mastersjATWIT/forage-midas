package com.midas.core.controllers;

import com.midas.core.models.Balance;
import com.midas.core.models.User;
import com.midas.core.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class BalanceController {
    
    private static final Logger logger = LoggerFactory.getLogger(BalanceController.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/balance")
    public Balance getBalance(@RequestParam Long userId) {
        logger.info("Received balance request for user ID: {}", userId);
        
        return userRepository.findById(userId)
                .map(user -> new Balance(user.getBalance()))
                .orElseGet(() -> {
                    logger.warn("User not found with ID: {}", userId);
                    return new Balance(BigDecimal.ZERO);
                });
    }
}