package com.midas.core.services;

import com.midas.core.models.Transaction;
import com.midas.core.models.TransactionRecord;
import com.midas.core.models.User;
import com.midas.core.repositories.TransactionRecordRepository;
import com.midas.core.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionRecordRepository transactionRecordRepository;
    
    @Autowired
    private IncentiveService incentiveService;
    
    @KafkaListener(topics = "transactions", groupId = "midas-group")
    @Transactional
    public void processTransaction(Transaction transaction) {
        logger.info("Processing transaction: {}", transaction.getTransactionId());
        
        // Validate transaction
        if (isValidTransaction(transaction)) {
            // Record the transaction and update balances
            recordTransactionWithIncentive(transaction);
        } else {
            logger.warn("Invalid transaction detected: {}", transaction.getTransactionId());
        }
    }
    
    private boolean isValidTransaction(Transaction transaction) {
        // Check if both sender and recipient exist
        Optional<User> senderOpt = userRepository.findById(transaction.getSenderId());
        Optional<User> recipientOpt = userRepository.findById(transaction.getRecipientId());
        
        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            logger.warn("Invalid sender or recipient ID");
            return false;
        }
        
        // Check if sender has sufficient balance
        User sender = senderOpt.get();
        if (sender.getBalance().compareTo(transaction.getAmount()) < 0) {
            logger.warn("Insufficient balance for user: {}", sender.getUsername());
            return false;
        }
        
        return true;
    }
    
    @Transactional
    public void recordTransactionWithIncentive(Transaction transaction) {
        User sender = userRepository.findById(transaction.getSenderId()).get();
        User recipient = userRepository.findById(transaction.getRecipientId()).get();
        
        // Get incentive amount from the API
        BigDecimal incentiveAmount = incentiveService.getIncentiveAmount(transaction);
        
        // Create and save transaction record with incentive
        TransactionRecord record = new TransactionRecord(transaction, sender, recipient, incentiveAmount);
        transactionRecordRepository.save(record);
        
        // Update balances
        // Sender only loses the transaction amount (not the incentive)
        sender.setBalance(sender.getBalance().subtract(transaction.getAmount()));
        
        // Recipient gets both the transaction amount and the incentive
        recipient.setBalance(recipient.getBalance()
                .add(transaction.getAmount())
                .add(incentiveAmount));
        
        // Save updated users
        userRepository.save(sender);
        userRepository.save(recipient);
        
        logger.info("Transaction processed successfully: {}, with incentive: {}", 
                transaction.getTransactionId(), incentiveAmount);
    }
}