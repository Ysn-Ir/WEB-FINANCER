package com.smartfin.backend.service;

import com.smartfin.backend.model.Account;
import com.smartfin.backend.model.Transaction;
import com.smartfin.backend.repository.AccountRepository;
import com.smartfin.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private com.smartfin.backend.repository.UserRepository userRepository;

    @Transactional
    public Transaction createTransaction(Transaction transaction, String username) {
        // Find User
        com.smartfin.backend.model.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        transaction.setUser(user);

        // Handle Account association
        Account account = transaction.getAccount();
        if (account == null) {
            // Find default/first account for user
            List<Account> accounts = accountRepository.findByUserUsername(username);
            if (accounts.isEmpty()) {
                // Create a default account if none exists (Auto-seeding for safety)
                Account newAccount = new Account();
                newAccount.setName("Main Account");
                newAccount.setBalance(BigDecimal.ZERO);
                newAccount.setType("CHECKING");
                newAccount.setUser(user);
                account = accountRepository.save(newAccount);
            } else {
                account = accounts.get(0);
            }
            transaction.setAccount(account);
        } else {
            // Fetch fresh to ensure it belongs to user? For MVP assuming ID is enough if
            // present
            account = accountRepository.findById(account.getId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));
        }

        // Validate Account belongs to user (Security check)
        if (!account.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized account access");
        }

        if ("EXPENSE".equals(transaction.getType())) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        } else if ("INCOME".equals(transaction.getType())) {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        }

        accountRepository.save(account);
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionsByUser(String username) {
        return transactionRepository.findByAccountUserUsername(username);
    }

    public List<Transaction> getTransactionsByAccount(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    @Transactional
    public Transaction updateTransaction(Long id, Transaction transactionUpdates, String username) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!existingTransaction.getAccount().getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        // Revert old balance effect
        Account account = existingTransaction.getAccount();
        if ("EXPENSE".equals(existingTransaction.getType())) {
            account.setBalance(account.getBalance().add(existingTransaction.getAmount()));
        } else if ("INCOME".equals(existingTransaction.getType())) {
            account.setBalance(account.getBalance().subtract(existingTransaction.getAmount()));
        }

        // Apply properties from updates
        existingTransaction.setAmount(transactionUpdates.getAmount());
        existingTransaction.setCategory(transactionUpdates.getCategory());
        existingTransaction.setDescription(transactionUpdates.getDescription());
        existingTransaction.setType(transactionUpdates.getType());
        existingTransaction.setDate(transactionUpdates.getDate());

        // Apply new balance effect
        if ("EXPENSE".equals(existingTransaction.getType())) {
            account.setBalance(account.getBalance().subtract(existingTransaction.getAmount()));
        } else if ("INCOME".equals(existingTransaction.getType())) {
            account.setBalance(account.getBalance().add(existingTransaction.getAmount()));
        }

        accountRepository.save(account);
        return transactionRepository.save(existingTransaction);
    }

    @Transactional
    public void deleteTransaction(Long id, String username) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!existingTransaction.getAccount().getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        // Revert balance effect
        Account account = existingTransaction.getAccount();
        if ("EXPENSE".equals(existingTransaction.getType())) {
            account.setBalance(account.getBalance().add(existingTransaction.getAmount()));
        } else if ("INCOME".equals(existingTransaction.getType())) {
            account.setBalance(account.getBalance().subtract(existingTransaction.getAmount()));
        }
        accountRepository.save(account);

        transactionRepository.delete(existingTransaction);
    }
}
