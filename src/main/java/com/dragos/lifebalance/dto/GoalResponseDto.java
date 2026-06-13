package com.dragos.lifebalance.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class GoalResponseDto {

    private Integer id;
    private String title;
    private BigDecimal targetAmount;
    private BigDecimal savedAmount;
    private BigDecimal remainingAmount;
    private Double progress;
    private Double remainingPercent;
    private BigDecimal estimatedMonthly;
    private BigDecimal allocationPercent;
    private Boolean active;
    private LocalDate deadline;
}