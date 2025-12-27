package com.smartfin.backend.repository;

import com.smartfin.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountUserUsername(String username);

    List<Transaction> findByAccountId(Long accountId);
}
