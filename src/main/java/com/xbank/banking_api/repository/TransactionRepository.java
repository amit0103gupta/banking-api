package com.xbank.banking_api.repository;

import com.xbank.banking_api.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE " +
           "t.fromAccount.accountNumber = :accountNumber OR " +
           "t.toAccount.accountNumber = :accountNumber " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findAllByAccountNumber(String accountNumber);
}
