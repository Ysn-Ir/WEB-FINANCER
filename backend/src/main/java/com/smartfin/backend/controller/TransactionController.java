package com.smartfin.backend.controller;

import com.smartfin.backend.model.Transaction;
import com.smartfin.backend.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:4200")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction,
            java.security.Principal principal) {
        return ResponseEntity.ok(transactionService.createTransaction(transaction, principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions(java.security.Principal principal) {
        return ResponseEntity.ok(transactionService.getTransactionsByUser(principal.getName()));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactionsByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccount(accountId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody Transaction transaction,
            java.security.Principal principal) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, transaction, principal.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id, java.security.Principal principal) {
        transactionService.deleteTransaction(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
