package com.smartfin.backend.controller;

import com.smartfin.backend.model.Goal;
import com.smartfin.backend.repository.GoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@CrossOrigin(origins = "http://localhost:4200")
public class GoalController {

    @Autowired
    private com.smartfin.backend.service.GoalService goalService;

    @GetMapping
    public List<Goal> getAllGoals(java.security.Principal principal) {
        return goalService.getGoalsForUser(principal.getName());
    }

    @PostMapping
    public Goal createGoal(@RequestBody Goal goal, java.security.Principal principal) {
        return goalService.createGoal(goal, principal.getName());
    }

    @PutMapping("/{id}")
    public Goal updateGoal(@PathVariable Long id, @RequestBody Goal goal, java.security.Principal principal) {
        return goalService.updateGoal(id, goal, principal.getName());
    }

    @DeleteMapping("/{id}")
    public void deleteGoal(@PathVariable Long id, java.security.Principal principal) {
        goalService.deleteGoal(id, principal.getName());
    }
}
