package com.dragos.lifebalance.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ExpenseResponseDto {

    private Integer id;
    private Integer categoryId;
    private String categoryName;
    private String title;
    private BigDecimal amount;
    private LocalDate dateSpent;
    private String notes;
    private LocalDateTime createdAt;
}