package com.dragos.lifebalance.service;

import com.dragos.lifebalance.dto.GoalRequestDto;
import com.dragos.lifebalance.dto.GoalResponseDto;
import com.dragos.lifebalance.dto.GoalUpdateRequestDto;
import com.dragos.lifebalance.entity.Goal;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.exceptions.NotFoundException;
import com.dragos.lifebalance.repository.GoalAllocationRepository;
import com.dragos.lifebalance.repository.GoalRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalAllocationRepository goalAllocationRepository;

    public GoalService(
            GoalRepository goalRepository,
            GoalAllocationRepository goalAllocationRepository
    ) {
        this.goalRepository = goalRepository;
        this.goalAllocationRepository = goalAllocationRepository;
    }

    public List<GoalResponseDto> getForUser(User user) {
        List<Goal> goals = goalRepository.findByUser_Id(user.getId());

        List<GoalResponseDto> result = new ArrayList<>();

        for (Goal goal : goals) {
            result.add(mapToDto(goal));
        }

        return result;
    }

    @Transactional
    public GoalResponseDto createGoal(User user, GoalRequestDto request) {
        validateTotalAllocationPercent(
                user,
                null,
                request.getAllocationPercent()
        );

        Goal goal = new Goal();

        goal.setUser(user);
        goal.setTitle(request.getTitle());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setDeadline(request.getDeadline());
        goal.setActive(true);
        goal.setAllocationPercent(request.getAllocationPercent());

        Goal savedGoal = goalRepository.save(goal);

        return mapToDto(savedGoal);
    }

    @Transactional
    public GoalResponseDto update(
            User user,
            Integer goalId,
            GoalUpdateRequestDto request
    ) {
        Goal goal = getGoalForUser(user, goalId);

        validateTotalAllocationPercent(
                user,
                goalId,
                request.getAllocationPercent()
        );

        goal.setTitle(request.getTitle());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setDeadline(request.getDeadline());
        goal.setAllocationPercent(request.getAllocationPercent());

        if (request.getActive() != null) {
            goal.setActive(request.getActive());
        }

        Goal savedGoal = goalRepository.save(goal);

        return mapToDto(savedGoal);
    }

    @Transactional
    public void delete(User user, Integer goalId) {
        Goal goal = getGoalForUser(user, goalId);

        goalAllocationRepository.deleteByGoal_Id(goalId);

        goalRepository.delete(goal);
    }

    private Goal getGoalForUser(User user, Integer goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new NotFoundException("Goal not found"));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Goal not found");
        }

        return goal;
    }

    private void validateTotalAllocationPercent(
            User user,
            Integer currentGoalId,
            BigDecimal newPercent
    ) {
        if (newPercent == null
                || newPercent.compareTo(BigDecimal.ZERO) <= 0
                || newPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Invalid percent");
        }

        List<Goal> goals = goalRepository.findByUser_Id(user.getId());

        BigDecimal existingPercent = BigDecimal.ZERO;

        for (Goal goal : goals) {
            if (currentGoalId != null && goal.getId().equals(currentGoalId)) {
                continue;
            }

            BigDecimal percent = goal.getAllocationPercent();

            if (percent == null) {
                percent = BigDecimal.ZERO;
            }

            existingPercent = existingPercent.add(percent);
        }

        BigDecimal total = existingPercent.add(newPercent);

        if (total.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Total percent cannot exceed 100%");
        }
    }

    private GoalResponseDto mapToDto(Goal goal) {
        GoalResponseDto dto = new GoalResponseDto();

        dto.setId(goal.getId());
        dto.setTitle(goal.getTitle());
        dto.setTargetAmount(goal.getTargetAmount());
        dto.setDeadline(goal.getDeadline());
        dto.setActive(goal.getActive());
        dto.setAllocationPercent(goal.getAllocationPercent());

        BigDecimal savedAmount = goalAllocationRepository.sumAllocatedForGoal(
                goal.getId(),
                goal.getUser().getId()
        );

        dto.setSavedAmount(savedAmount);

        BigDecimal targetAmount = goal.getTargetAmount();

        if (targetAmount != null && targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal progress = savedAmount
                    .divide(targetAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            dto.setProgress(progress.doubleValue());
        } else {
            dto.setProgress(0.0);
        }

        BigDecimal remainingAmount = targetAmount.subtract(savedAmount);
        remainingAmount = remainingAmount.max(BigDecimal.ZERO);

        dto.setRemainingAmount(remainingAmount);
        dto.setRemainingPercent(Math.max(0.0, 100.0 - dto.getProgress()));

        if (goal.getDeadline() == null
                || !goal.getDeadline().isAfter(LocalDate.now())
                || remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            dto.setEstimatedMonthly(null);
        } else {
            int months = estimateMonths(goal.getDeadline());

            BigDecimal estimatedMonthly = remainingAmount.divide(
                    BigDecimal.valueOf(months),
                    2,
                    RoundingMode.HALF_UP
            );

            dto.setEstimatedMonthly(estimatedMonthly);
        }

        return dto;
    }

    private int estimateMonths(LocalDate deadline) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), deadline);

        if (days <= 0) {
            return 0;
        }

        double months = days / 30.44;

        return Math.max(1, (int) Math.ceil(months));
    }
}