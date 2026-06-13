package com.dragos.lifebalance.controller;

import com.dragos.lifebalance.dto.GoalRequestDto;
import com.dragos.lifebalance.dto.GoalResponseDto;
import com.dragos.lifebalance.dto.GoalUpdateRequestDto;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public ResponseEntity<List<GoalResponseDto>> getGoals(
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                goalService.getForUser(user)
        );
    }

    @PostMapping
    public ResponseEntity<GoalResponseDto> createGoal(
            Authentication authentication,
            @RequestBody @Valid GoalRequestDto request
    ) {
        User user = (User) authentication.getPrincipal();

        GoalResponseDto response = goalService.createGoal(user, request);

        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponseDto> updateGoal(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestBody @Valid GoalUpdateRequestDto request
    ) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                goalService.update(user, id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(
            Authentication authentication,
            @PathVariable Integer id
    ) {
        User user = (User) authentication.getPrincipal();

        goalService.delete(user, id);

        return ResponseEntity.noContent().build();
    }
}