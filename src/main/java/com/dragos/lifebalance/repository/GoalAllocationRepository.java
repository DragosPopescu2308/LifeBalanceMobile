package com.dragos.lifebalance.repository;

import com.dragos.lifebalance.entity.GoalAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface GoalAllocationRepository extends JpaRepository<GoalAllocation, Integer> {

    @Query("""
        SELECT COALESCE(SUM(a.amount), 0)
        FROM GoalAllocation a
        WHERE a.goal.id = :goalId
          AND a.user.id = :userId
    """)
    BigDecimal sumAllocatedForGoal(Integer goalId, Integer userId);

    @Query("""
        SELECT COALESCE(SUM(a.amount), 0)
        FROM GoalAllocation a
        WHERE a.user.id = :userId
    """)
    BigDecimal sumAllocatedForUser(Integer userId);

    List<GoalAllocation> findBySavingsTransaction_Id(Integer savingsTransactionId);

    void deleteByGoal_Id(Integer goalId);
}