package com.dragos.lifebalance.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DashboardTrendPointDto {

    private String month;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal savings;
    private BigDecimal net;
}