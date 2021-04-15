package com.phamthehuy.doan.repository;

import com.phamthehuy.doan.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByCustomer_Email(String email);
    Transaction findByToken(String token);
}
