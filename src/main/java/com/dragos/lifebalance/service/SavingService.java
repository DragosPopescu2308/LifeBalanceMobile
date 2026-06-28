package com.dragos.lifebalance.service;

import com.dragos.lifebalance.dto.SavingSettingDto;
import com.dragos.lifebalance.entity.Income;
import com.dragos.lifebalance.entity.SavingSetting;
import com.dragos.lifebalance.entity.SavingsTransaction;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.repository.SavingSettingRepository;
import com.dragos.lifebalance.repository.SavingsTransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.dragos.lifebalance.dto.SavingSummaryDto;
import com.dragos.lifebalance.repository.GoalAllocationRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
public class SavingService {

    private final SavingSettingRepository savingSettingRepository;
    private final SavingsTransactionRepository savingsTransactionRepository;
    private final GoalAllocationService goalAllocationService;
    private final GoalAllocationRepository goalAllocationRepository;

    public SavingService(
            SavingSettingRepository savingSettingRepository,
            SavingsTransactionRepository savingsTransactionRepository,
            GoalAllocationService goalAllocationService,
            GoalAllocationRepository goalAllocationRepository
    ) {
        this.savingSettingRepository = savingSettingRepository;
        this.savingsTransactionRepository = savingsTransactionRepository;
        this.goalAllocationService = goalAllocationService;
        this.goalAllocationRepository = goalAllocationRepository;
    }

    public SavingSettingDto getSettings(User user) {
        SavingSetting setting = getOrCreateSettings(user);
        return mapToDto(setting);
    }

    @Transactional
    public SavingSettingDto updateSettings(
            User user,
            SavingSettingDto request
    ) {
        SavingSetting setting = getOrCreateSettings(user);

        setting.setPercentage(request.getPercentage());
        setting.setActive(request.getActive());

        SavingSetting savedSetting =
                savingSettingRepository.save(setting);

        return mapToDto(savedSetting);
    }

    public BigDecimal getMonthlyTotal(User user, String month) {
        YearMonth yearMonth = YearMonth.parse(month);

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.plusMonths(1).atDay(1);

        return savingsTransactionRepository
                .findByUser_IdAndDateSavedBetween(
                        user.getId(),
                        start,
                        end
                )
                .stream()
                .map(transaction -> transaction.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getRemainingSavings(User user) {
        return getSummary(user).getRemainingSavings();
    }

    public SavingSummaryDto getSummary(User user) {
        BigDecimal totalSavings =
                savingsTransactionRepository.sumSavingsForUser(user.getId());

        BigDecimal allocatedSavings =
                goalAllocationRepository.sumAllocatedForUser(user.getId());

        if (totalSavings == null) {
            totalSavings = BigDecimal.ZERO;
        }

        if (allocatedSavings == null) {
            allocatedSavings = BigDecimal.ZERO;
        }

        BigDecimal remainingSavings =
                totalSavings.subtract(allocatedSavings);

        if (remainingSavings.compareTo(BigDecimal.ZERO) < 0) {
            remainingSavings = BigDecimal.ZERO;
        }

        return new SavingSummaryDto(
                totalSavings,
                allocatedSavings,
                remainingSavings
        );
    }

    @Transactional
    public void processSavings(Income income) {
        if (income == null) {
            return;
        }

        if (income.getUser() == null) {
            return;
        }

        if (income.getId() == null) {
            return;
        }

        if (savingsTransactionRepository.existsByIncome_Id(income.getId())) {
            return;
        }

        User user = income.getUser();

        SavingSetting setting = getOrCreateSettings(user);

        if (!Boolean.TRUE.equals(setting.getActive())) {
            return;
        }

        Double rawPercentage = setting.getPercentage();

        if (rawPercentage == null || rawPercentage <= 0) {
            return;
        }

        BigDecimal percentage = BigDecimal.valueOf(rawPercentage)
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

        BigDecimal incomeAmount = income.getAmount();

        if (incomeAmount == null) {
            incomeAmount = BigDecimal.ZERO;
        }

        BigDecimal savedAmount = incomeAmount
                .multiply(percentage)
                .setScale(2, RoundingMode.HALF_UP);

        if (savedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        SavingsTransaction transaction = new SavingsTransaction();

        transaction.setUser(user);
        transaction.setIncome(income);
        transaction.setAmount(savedAmount);
        transaction.setDateSaved(income.getDateReceived());
        transaction.setCreatedAt(LocalDateTime.now());

        SavingsTransaction savedTransaction =
                savingsTransactionRepository.save(transaction);

        goalAllocationService.allocateAuto(savedTransaction);
    }

    private SavingSetting getOrCreateSettings(User user) {
        return savingSettingRepository.findById(user.getId())
                .orElseGet(() -> {
                    SavingSetting setting = new SavingSetting();

                    setting.setUser(user);
                    setting.setPercentage(10.0);
                    setting.setActive(true);

                    return savingSettingRepository.save(setting);
                });
    }

    private SavingSettingDto mapToDto(SavingSetting setting) {
        SavingSettingDto dto = new SavingSettingDto();

        dto.setPercentage(setting.getPercentage());
        dto.setActive(setting.getActive());

        return dto;
    }
}