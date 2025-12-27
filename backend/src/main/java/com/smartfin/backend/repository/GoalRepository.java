package com.smartfin.backend.repository;

import com.smartfin.backend.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserId(Long userId);

    List<Goal> findByUserUsername(String username);
}
