package com.dragos.lifebalance.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class IncomeResponseDto {

    private Integer id;
    private Integer categoryId;
    private String categoryName;
    private String title;
    private BigDecimal amount;
    private LocalDate dateReceived;
    private String notes;
    private LocalDateTime createdAt;
}