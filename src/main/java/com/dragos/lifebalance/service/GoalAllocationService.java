package com.dragos.lifebalance.service;

import com.dragos.lifebalance.entity.Goal;
import com.dragos.lifebalance.entity.GoalAllocation;
import com.dragos.lifebalance.entity.SavingsTransaction;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.repository.GoalAllocationRepository;
import com.dragos.lifebalance.repository.GoalRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GoalAllocationService {

    private final GoalRepository goalRepository;
    private final GoalAllocationRepository goalAllocationRepository;

    public GoalAllocationService(
            GoalRepository goalRepository,
            GoalAllocationRepository goalAllocationRepository
    ) {
        this.goalRepository = goalRepository;
        this.goalAllocationRepository = goalAllocationRepository;
    }

    @Transactional
    public void allocateAuto(SavingsTransaction savingsTransaction) {
        User user = savingsTransaction.getUser();
        BigDecimal totalSavings = savingsTransaction.getAmount();

        List<Goal> goals = goalRepository.findByUser_Id(user.getId());

        for (Goal goal : goals) {
            if (!Boolean.TRUE.equals(goal.getActive())) {
                continue;
            }

            BigDecimal percent = goal.getAllocationPercent();

            if (percent == null || percent.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal amountForGoal = totalSavings
                    .multiply(percent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (amountForGoal.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            GoalAllocation allocation = new GoalAllocation();

            allocation.setUser(user);
            allocation.setGoal(goal);
            allocation.setSavingsTransaction(savingsTransaction);
            allocation.setAmount(amountForGoal);
            allocation.setCreatedAt(LocalDateTime.now());

            goalAllocationRepository.save(allocation);
        }
    }
}