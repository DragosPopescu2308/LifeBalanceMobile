package com.dragos.lifebalance.repository;

import com.dragos.lifebalance.entity.SavingsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SavingsTransactionRepository
        extends JpaRepository<SavingsTransaction, Integer> {

    List<SavingsTransaction> findByUser_IdAndDateSavedBetween(
            Integer userId,
            LocalDate start,
            LocalDate end
    );

    boolean existsByIncome_Id(Integer incomeId);

    List<SavingsTransaction> findByUser_Id(Integer userId);

    @Query("""
        SELECT COALESCE(SUM(s.amount), 0)
        FROM SavingsTransaction s
        WHERE s.user.id = :userId
    """)
    BigDecimal sumSavingsForUser(Integer userId);

    List<SavingsTransaction> findByUser_IdOrderByDateSavedAsc(Integer userId);
}