package com.dragos.lifebalance.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class DashboardResponseDto {

    private String month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal totalSavings;
    private BigDecimal net;

    private List<CategoryTotalDto> topExpenseCategories;

    @Getter
    @Setter
    public static class CategoryTotalDto {
        private Integer categoryId;
        private String categoryName;
        private BigDecimal total;
    }
}