package com.dragos.lifebalance.repository;

import com.dragos.lifebalance.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    List<Expense> findByUser_Id(Integer userId);

    List<Expense> findByUser_IdAndDateSpentBetween(
            Integer userId,
            LocalDate start,
            LocalDate end
    );

    @Query("""
        select e.category.id, e.category.name, sum(e.amount)
        from Expense e
        where e.user.id = :userId
          and e.dateSpent >= :start
          and e.dateSpent < :end
          and e.category is not null
        group by e.category.id, e.category.name
        order by sum(e.amount) desc
    """)
    List<Object[]> sumByCategoryForMonth(
            @Param("userId") Integer userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}