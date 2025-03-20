package com.midas.core.repositories;

import com.midas.core.models.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {
    // Additional query methods can be added here if needed
}