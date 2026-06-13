package com.dragos.lifebalance.repository;

import com.dragos.lifebalance.entity.IncomeAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IncomeAttachmentRepository
        extends JpaRepository<IncomeAttachment, Integer> {

    List<IncomeAttachment> findByIncome_Id(Integer incomeId);

    @Query("""
        select attachment from IncomeAttachment attachment
        join fetch attachment.income income
        join fetch income.user user
        where attachment.id = :id
    """)
    Optional<IncomeAttachment> findByIdWithIncomeAndUser(
            @Param("id") Integer id
    );
}