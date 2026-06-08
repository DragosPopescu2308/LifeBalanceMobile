package com.dragos.lifebalance.repository;

import com.dragos.lifebalance.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Integer> {

    List<Income> findByUser_Id(Integer userId);

    List<Income> findByUser_IdAndDateReceivedBetween(
            Integer userId,
            LocalDate start,
            LocalDate end
    );
}