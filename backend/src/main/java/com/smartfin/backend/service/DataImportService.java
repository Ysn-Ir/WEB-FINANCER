package com.smartfin.backend.service;

import com.smartfin.backend.model.Account;
import com.smartfin.backend.model.Asset;
import com.smartfin.backend.model.Transaction;
import com.smartfin.backend.model.User;
import com.smartfin.backend.repository.AccountRepository;
import com.smartfin.backend.repository.AssetRepository;
import com.smartfin.backend.repository.TransactionRepository;
import com.smartfin.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataImportService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void importTransactions(MultipartFile file, String username) throws Exception {
        System.out.println("Starting transaction import for: " + username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get default account or create one
        List<Account> accounts = accountRepository.findByUserUsername(username);
        Account account;
        if (accounts.isEmpty()) {
            System.out.println("No accounts found, creating default.");
            account = new Account();
            account.setName("Main Import Account");
            account.setType("CHECKING");
            account.setBalance(BigDecimal.ZERO);
            account.setCurrency("USD");
            account.setUser(user);
            account = accountRepository.save(account);
        } else {
            account = accounts.get(0);
        }

        List<Transaction> transactions = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // Read header (and ignore potentially BOM)
            String line = br.readLine();
            if (line == null)
                return; // Empty file

            int lineCount = 0;
            while ((line = br.readLine()) != null) {
                lineCount++;
                String[] values = line.split(",");
                // Expects: Date,Description,Amount,Type,Category
                // 2024-01-01,Grocery,50.00,EXPENSE,Food

                if (values.length < 5) {
                    System.err.println("Line " + lineCount + " skipped: Insufficient columns. Content: " + line);
                    continue;
                }

                try {
                    Transaction t = new Transaction();
                    t.setDate(LocalDate.parse(values[0].trim().replace("\uFEFF", ""))); // Strip BOM if present on date
                    t.setDescription(values[1].trim());
                    t.setAmount(new BigDecimal(values[2].trim()));
                    t.setType(values[3].trim().toUpperCase());
                    t.setCategory(values[4].trim());
                    t.setAccount(account); // Link to account
                    t.setUser(user); // Link to user for direct access/security

                    transactions.add(t);
                } catch (Exception e) {
                    System.err.println("Error parsing line " + lineCount + ": " + line);
                    e.printStackTrace();
                    // Don't abort entire import for one bad line, but maybe rethrow if needed
                }
            }
        }

        System.out.println("Saving " + transactions.size() + " transactions...");
        try {
            transactionRepository.saveAll(transactions);
            System.out.println("Transaction import complete.");
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to save transactions to database.");
            e.printStackTrace();
            throw e; // Rethrow to let controller know
        }
    }

    @Transactional
    public void importAssets(MultipartFile file, String username) throws Exception {
        System.out.println("Starting asset import for: " + username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Asset> assets = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // Read header
            String line = br.readLine();
            if (line == null)
                return;

            int lineCount = 0;
            while ((line = br.readLine()) != null) {
                lineCount++;
                String[] values = line.split(",");
                // Expects: Symbol,Name,Type,Quantity,AvgCost
                // AAPL,Apple Inc,STOCK,10,150.00

                if (values.length < 5) {
                    System.err.println("Line " + lineCount + " skipped: Insufficient columns.");
                    continue;
                }

                try {
                    Asset a = new Asset();
                    a.setSymbol(values[0].trim().replace("\uFEFF", ""));
                    a.setName(values[1].trim());
                    a.setType(values[2].trim().toUpperCase());
                    a.setQuantity(new BigDecimal(values[3].trim()));
                    a.setAvgCost(new BigDecimal(values[4].trim()));
                    a.setCurrentPrice(new BigDecimal(values[4].trim())); // Default current to cost
                    a.setLastUpdate(LocalDateTime.now()); // Set timestamp
                    a.setUser(user);

                    assets.add(a);
                    System.out.println("Parsed asset: " + a.getSymbol());
                } catch (Exception e) {
                    System.err.println("Error parsing line " + lineCount + ": " + line);
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Saving " + assets.size() + " assets...");
        try {
            assetRepository.saveAll(assets);
            System.out.println("Asset import complete.");
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to save assets to database.");
            e.printStackTrace();
            throw e;
        }
    }
}
