package com.dragos.lifebalance.repository;

import com.dragos.lifebalance.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    List<Expense> findByUser_Id(Integer userId);

    List<Expense> findByUser_IdAndDateSpentBetween(
            Integer userId,
            LocalDate start,
            LocalDate end
    );
}