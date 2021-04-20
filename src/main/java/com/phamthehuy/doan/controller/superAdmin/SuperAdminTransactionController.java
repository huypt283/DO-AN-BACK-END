package com.phamthehuy.doan.controller.superAdmin;

import com.phamthehuy.doan.entity.Transaction;
import com.phamthehuy.doan.model.response.TransactionResponse;
import com.phamthehuy.doan.repository.TransactionRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/super-admin/transactions")
public class SuperAdminTransactionController {
    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping
    public ResponseEntity<?> listAllTransaction() {
        List<Transaction> transactions = transactionRepository.findAll();

        return new ResponseEntity<>(transactions.stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList()), HttpStatus.OK);
    }

    private TransactionResponse convertToTransactionResponse(Transaction transaction) {
        TransactionResponse transactionResponse = new TransactionResponse();
        BeanUtils.copyProperties(transaction, transactionResponse);
        transactionResponse.setEmail(transaction.getCustomer().getEmail());
        return transactionResponse;
    }
}
