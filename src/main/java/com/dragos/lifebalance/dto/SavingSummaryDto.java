package com.dragos.lifebalance.dto;

import java.math.BigDecimal;

public class SavingSummaryDto {

    private BigDecimal totalSavings;
    private BigDecimal allocatedSavings;
    private BigDecimal remainingSavings;

    public SavingSummaryDto() {
    }

    public SavingSummaryDto(
            BigDecimal totalSavings,
            BigDecimal allocatedSavings,
            BigDecimal remainingSavings
    ) {
        this.totalSavings = totalSavings;
        this.allocatedSavings = allocatedSavings;
        this.remainingSavings = remainingSavings;
    }

    public BigDecimal getTotalSavings() {
        return totalSavings;
    }

    public void setTotalSavings(BigDecimal totalSavings) {
        this.totalSavings = totalSavings;
    }

    public BigDecimal getAllocatedSavings() {
        return allocatedSavings;
    }

    public void setAllocatedSavings(BigDecimal allocatedSavings) {
        this.allocatedSavings = allocatedSavings;
    }

    public BigDecimal getRemainingSavings() {
        return remainingSavings;
    }

    public void setRemainingSavings(BigDecimal remainingSavings) {
        this.remainingSavings = remainingSavings;
    }
}