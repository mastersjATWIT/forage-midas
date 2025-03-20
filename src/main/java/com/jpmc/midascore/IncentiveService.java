package com.midas.core.services;

import com.midas.core.models.Incentive;
import com.midas.core.models.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class IncentiveService {
    
    private static final Logger logger = LoggerFactory.getLogger(IncentiveService.class);
    private static final String INCENTIVE_API_URL = "http://localhost:8080/incentive";
    
    @Autowired
    private RestTemplate restTemplate;
    
    public BigDecimal getIncentiveAmount(Transaction transaction) {
        try {
            logger.info("Requesting incentive for transaction: {}", transaction.getTransactionId());
            
            // Send the transaction to the incentive API
            ResponseEntity<Incentive> response = restTemplate.postForEntity(
                INCENTIVE_API_URL,
                transaction,
                Incentive.class
            );
            
            if (response.getBody() != null) {
                logger.info("Received incentive amount: {}", response.getBody().getAmount());
                return response.getBody().getAmount();
            } else {
                logger.warn("No incentive response body received");
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            logger.error("Error retrieving incentive amount: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
}