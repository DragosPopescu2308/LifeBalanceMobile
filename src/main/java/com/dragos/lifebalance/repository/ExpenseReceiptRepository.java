package com.dragos.lifebalance.repository;

import com.dragos.lifebalance.entity.ExpenseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExpenseReceiptRepository
        extends JpaRepository<ExpenseReceipt, Integer> {

    List<ExpenseReceipt> findByExpense_Id(Integer expenseId);

    @Query("""
        select receipt from ExpenseReceipt receipt
        join fetch receipt.expense expense
        join fetch expense.user user
        where receipt.id = :id
    """)
    Optional<ExpenseReceipt> findByIdWithExpenseAndUser(
            @Param("id") Integer id
    );
}