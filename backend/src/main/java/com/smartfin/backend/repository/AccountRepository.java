package com.smartfin.backend.repository;

import com.smartfin.backend.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserId(Long userId);

    List<Account> findByUserUsername(String username);
}
