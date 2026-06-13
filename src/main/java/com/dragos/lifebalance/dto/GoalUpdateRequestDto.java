package com.dragos.lifebalance.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class GoalUpdateRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be at most 100 characters")
    private String title;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be positive")
    private BigDecimal targetAmount;

    private LocalDate deadline;

    @NotNull(message = "Allocation percent is required")
    @DecimalMin(value = "0.01", message = "Allocation percent must be greater than 0")
    @DecimalMax(value = "100.00", message = "Allocation percent must be at most 100")
    private BigDecimal allocationPercent;

    private Boolean active;
}