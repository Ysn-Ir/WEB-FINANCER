package com.smartfin.backend.config;

import com.smartfin.backend.model.*;
import com.smartfin.backend.repository.*;
import com.smartfin.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            // Fix for "Packet for query is too large"
            try {
                jdbcTemplate.execute("SET GLOBAL max_allowed_packet = 67108864");
                System.out.println("Set max_allowed_packet to 64MB");
            } catch (Exception e) {
                System.err.println("Could not set max_allowed_packet: " + e.getMessage());
            }

            if (transactionRepository.count() == 0) {
                System.out.println("SEEDING DATABASE...");
                seedData();
                System.out.println("DATABASE SEEDING COMPLETE.");
            }
        } catch (Exception e) {
            System.err.println("ERROR SEEDING DATABASE:");
            e.printStackTrace();
            throw e; // Rethrow to stop startup if seeding fails critically
        }
    }

    private void seedData() {
        // 1. Create User (if not exists)
        User user;
        if (userRepository.findByUsername("admin").isEmpty()) {
            user = new User();
            user.setUsername("admin");
            user.setEmail("admin@smartfin.com");
            // NOTE: In a real scenario, use PasswordEncoder. Here we assume simple string
            // or pre-hashed.
            // If the AuthController uses BCrypt, we should hash it.
            // Since we can't easily inject the same encoder bean without potential circular
            // dep,
            // we'll assume standard "$2a$10$..." hash for "admin" or just plain text if
            // security config allows.
            // For now, setting "admin" as plain text (SecurityConfig basic auth might
            // expect encoded).
            // Let's rely on the fact that existing users might not exist.
            user.setPassword(passwordEncoder.encode("admin")); // Hash correctly
            // If SecurityConfig uses BCryptPasswordEncoder() directly, this plain text
            // won't work for login.
            // BUT: The goal is to seed data. We can update password later or use the
            // registration endpoint.
            // Let's look at SecurityConfig... it uses new BCryptPasswordEncoder().
            // So we really should hash it.
            // Simplest way: manually set a known hash for "admin".
            // "admin" hashed with bcrypt cost 10:
            // $2a$10$8.UnVuG9HHgffUDAlk8qfOpNaNx.e5n3.2j17/L6f2Gu0y0y2/..
            // Or just inject PasswordEncoder.
            // actually, let's just inject PasswordEncoder lazily or use UserService if
            // possible.
            // But to avoid complexity, I will just set a placeholder.
            // UPDATE: The user told me to "make me some data".
            // I'll try to use a simple hash generator if possible, or just set it and let
            // user register if it fails.
            // Wait, I can just inject PasswordEncoder!
            user.setFullName("Admin User");
            user = userRepository.save(user);
        } else {
            user = userRepository.findByUsername("admin").get();
        }

        // 2. Create Account
        Account account;
        if (accountRepository.count() == 0) {
            account = new Account();
            account.setName("Main Checking");
            account.setType("CHECKING");
            account.setCurrency("USD");
            account.setBalance(BigDecimal.valueOf(15000));
            account.setUser(user);
            account = accountRepository.save(account);
        } else {
            account = accountRepository.findAll().get(0);
        }

        // 3. Seed Assets
        if (assetRepository.count() == 0) {
            assetRepository.saveAll(Arrays.asList(
                    createAsset(user, "AAPL", "Apple Inc.", 15, 145.50),
                    createAsset(user, "TSLA", "Tesla Inc.", 5, 210.00),
                    createAsset(user, "BTC", "Bitcoin", 0.25, 35000.00),
                    createAsset(user, "ETH", "Ethereum", 2.0, 2100.00),
                    createAsset(user, "SPY", "S&P 500 ETF", 10, 410.00)));
        }

        // 4. Seed Goals
        if (goalRepository.count() == 0) {
            goalRepository.saveAll(Arrays.asList(
                    createGoal(user, "Emergency Fund", 15000, 5000, LocalDate.now().plusMonths(12)),
                    createGoal(user, "Dream Vacation", 5000, 1200, LocalDate.now().plusMonths(6)),
                    createGoal(user, "New MacBook", 3000, 3000, LocalDate.now().plusDays(5))));
        }

        // 5. Seed Transactions (Spread over last 6 months)
        if (transactionRepository.count() == 0) {
            // Expenses
            createTransaction(account, "Housing", 1500.00, "Rent Payment", "EXPENSE", LocalDate.now().minusDays(2));
            createTransaction(account, "Food", 125.50, "Grocery Run", "EXPENSE", LocalDate.now().minusDays(5));
            createTransaction(account, "Transport", 45.00, "Uber Ride", "EXPENSE", LocalDate.now().minusDays(10));
            createTransaction(account, "Entertainment", 24.99, "Netflix Subscription", "EXPENSE",
                    LocalDate.now().minusDays(15));

            // Previous Months
            createTransaction(account, "Housing", 1500.00, "Rent Payment", "EXPENSE", LocalDate.now().minusMonths(1));
            createTransaction(account, "Food", 450.00, "Monthly Groceries", "EXPENSE", LocalDate.now().minusMonths(1));
            createTransaction(account, "Transport", 120.00, "Gas", "EXPENSE", LocalDate.now().minusMonths(1));

            // Income
            createTransaction(account, "Salary", 5000.00, "Monthly Salary", "INCOME", LocalDate.now().minusDays(1));
            createTransaction(account, "Salary", 5000.00, "Monthly Salary", "INCOME", LocalDate.now().minusMonths(1));
            createTransaction(account, "Freelance", 850.00, "Consulting Project", "INCOME",
                    LocalDate.now().minusDays(12));
            createTransaction(account, "Dividends", 120.50, "Stock Dividends", "INCOME", LocalDate.now().minusDays(20));
        }
    }

    private Asset createAsset(User user, String symbol, String name, double quantity, double avgCost) {
        Asset a = new Asset();
        a.setUser(user);
        a.setSymbol(symbol);
        a.setName(name);
        a.setType("STOCK"); // Defaulting to STOCK for simplicity
        a.setQuantity(BigDecimal.valueOf(quantity));
        a.setAvgCost(BigDecimal.valueOf(avgCost));
        return a;
    }

    private Goal createGoal(User user, String name, double target, double current, LocalDate deadline) {
        Goal g = new Goal();
        g.setUser(user);
        g.setName(name);
        g.setTargetAmount(BigDecimal.valueOf(target));
        g.setCurrentAmount(BigDecimal.valueOf(current));
        g.setDeadline(deadline);
        return g;
    }

    private void createTransaction(Account account, String category, double amount, String desc, String type,
            LocalDate date) {
        Transaction t = new Transaction();
        t.setAccount(account);
        t.setCategory(category);
        t.setAmount(BigDecimal.valueOf(amount));
        t.setDescription(desc);
        t.setType(type);
        // Transaction entity expects LocalDateTime
        t.setDate(date);
        transactionRepository.save(t);
    }
}
