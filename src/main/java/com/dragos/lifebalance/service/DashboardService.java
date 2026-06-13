package com.dragos.lifebalance.service;

import com.dragos.lifebalance.dto.DashboardResponseDto;
import com.dragos.lifebalance.dto.DashboardTrendPointDto;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.repository.ExpenseRepository;
import com.dragos.lifebalance.repository.IncomeRepository;
import com.dragos.lifebalance.repository.SavingsTransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final SavingsTransactionRepository savingsTransactionRepository;

    public DashboardService(
            IncomeRepository incomeRepository,
            ExpenseRepository expenseRepository,
            SavingsTransactionRepository savingsTransactionRepository
    ) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.savingsTransactionRepository = savingsTransactionRepository;
    }

    public DashboardResponseDto getMonthlyDashboard(User user, String month) {
        YearMonth yearMonth;

        if (month == null || month.isBlank()) {
            yearMonth = YearMonth.now();
        } else {
            yearMonth = YearMonth.parse(month);
        }

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.plusMonths(1).atDay(1);

        BigDecimal totalIncome = incomeRepository
                .findByUser_IdAndDateReceivedBetween(user.getId(), start, end)
                .stream()
                .map(income -> income.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = expenseRepository
                .findByUser_IdAndDateSpentBetween(user.getId(), start, end)
                .stream()
                .map(expense -> expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSavings = savingsTransactionRepository
                .findByUser_IdAndDateSavedBetween(
                        user.getId(),
                        start,
                        end
                )
                .stream()
                .map(transaction -> transaction.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal net = totalIncome
                .subtract(totalExpense)
                .subtract(totalSavings);

        List<DashboardResponseDto.CategoryTotalDto> topCategories =
                new ArrayList<>();

        List<Object[]> rows = expenseRepository.sumByCategoryForMonth(
                user.getId(),
                start,
                end
        );

        for (Object[] row : rows) {
            DashboardResponseDto.CategoryTotalDto dto =
                    new DashboardResponseDto.CategoryTotalDto();

            dto.setCategoryId((Integer) row[0]);
            dto.setCategoryName((String) row[1]);
            dto.setTotal((BigDecimal) row[2]);

            topCategories.add(dto);
        }

        DashboardResponseDto response = new DashboardResponseDto();

        response.setMonth(yearMonth.toString());
        response.setTotalIncome(totalIncome);
        response.setTotalExpense(totalExpense);
        response.setTotalSavings(totalSavings);
        response.setNet(net);
        response.setTopExpenseCategories(topCategories);

        return response;
    }

    public List<DashboardTrendPointDto> getTrend(
            User user,
            int months,
            String endMonth
    ) {
        int safeMonths = Math.max(1, Math.min(months, 24));

        YearMonth endYearMonth;

        if (endMonth == null || endMonth.isBlank()) {
            endYearMonth = YearMonth.now();
        } else {
            endYearMonth = YearMonth.parse(endMonth);
        }

        List<DashboardTrendPointDto> result = new ArrayList<>();

        for (int i = safeMonths - 1; i >= 0; i--) {
            YearMonth currentMonth = endYearMonth.minusMonths(i);

            LocalDate start = currentMonth.atDay(1);
            LocalDate end = currentMonth.plusMonths(1).atDay(1);

            BigDecimal income = incomeRepository
                    .findByUser_IdAndDateReceivedBetween(
                            user.getId(),
                            start,
                            end
                    )
                    .stream()
                    .map(item -> item.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = expenseRepository
                    .findByUser_IdAndDateSpentBetween(
                            user.getId(),
                            start,
                            end
                    )
                    .stream()
                    .map(item -> item.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal savings = savingsTransactionRepository
                    .findByUser_IdAndDateSavedBetween(
                            user.getId(),
                            start,
                            end
                    )
                    .stream()
                    .map(transaction -> transaction.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);


            BigDecimal net = income
                    .subtract(expense)
                    .subtract(savings);

            DashboardTrendPointDto point = new DashboardTrendPointDto();

            point.setMonth(currentMonth.toString());
            point.setIncome(income);
            point.setExpense(expense);
            point.setSavings(savings);
            point.setNet(net);

            result.add(point);
        }

        return result;
    }
}