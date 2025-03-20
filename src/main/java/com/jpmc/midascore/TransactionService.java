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
    
    @KafkaListener(topics = "transactions", groupId = "midas-group")
    @Transactional
    public void processTransaction(Transaction transaction) {
        logger.info("Processing transaction: {}", transaction.getTransactionId());
        
        // Validate transaction
        if (isValidTransaction(transaction)) {
            // Record the transaction and update balances
            recordTransaction(transaction);
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
    public void recordTransaction(Transaction transaction) {
        User sender = userRepository.findById(transaction.getSenderId()).get();
        User recipient = userRepository.findById(transaction.getRecipientId()).get();
        
        // Create and save transaction record
        TransactionRecord record = new TransactionRecord(transaction, sender, recipient);
        transactionRecordRepository.save(record);
        
        // Update balances
        sender.setBalance(sender.getBalance().subtract(transaction.getAmount()));
        recipient.setBalance(recipient.getBalance().add(transaction.getAmount()));
        
        // Save updated users
        userRepository.save(sender);
        userRepository.save(recipient);
        
        logger.info("Transaction processed successfully: {}", transaction.getTransactionId());
    }
}