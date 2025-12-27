package com.smartfin.backend.service;

import com.smartfin.backend.model.Goal;
import com.smartfin.backend.model.User;
import com.smartfin.backend.repository.GoalRepository;
import com.smartfin.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Goal> getGoalsForUser(String username) {
        return goalRepository.findByUserUsername(username);
    }

    public Goal createGoal(Goal goal, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        goal.setUser(user);
        return goalRepository.save(goal);
    }

    public Goal updateGoal(Long id, Goal goalUpdates, String username) {
        Goal existingGoal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (!existingGoal.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        existingGoal.setName(goalUpdates.getName());
        existingGoal.setTargetAmount(goalUpdates.getTargetAmount());
        existingGoal.setCurrentAmount(goalUpdates.getCurrentAmount());
        existingGoal.setDeadline(goalUpdates.getDeadline());

        return goalRepository.save(existingGoal);
    }

    public void deleteGoal(Long id, String username) {
        Goal existingGoal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (!existingGoal.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        goalRepository.delete(existingGoal);
    }
}
