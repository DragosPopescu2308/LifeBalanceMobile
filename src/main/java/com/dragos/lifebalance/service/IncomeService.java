package com.dragos.lifebalance.service;

import com.dragos.lifebalance.dto.IncomeCreateRequestDto;
import com.dragos.lifebalance.dto.IncomeResponseDto;
import com.dragos.lifebalance.dto.IncomeUpdateRequestDto;
import com.dragos.lifebalance.entity.Category;
import com.dragos.lifebalance.entity.Income;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.entity.enums.CategoryType;
import com.dragos.lifebalance.exceptions.NotFoundException;
import com.dragos.lifebalance.repository.CategoryRepository;
import com.dragos.lifebalance.repository.IncomeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final SavingService savingService;

    public IncomeService(
            IncomeRepository incomeRepository,
            CategoryRepository categoryRepository,
            SavingService savingService
    ) {
        this.incomeRepository = incomeRepository;
        this.categoryRepository = categoryRepository;
        this.savingService = savingService;
    }

    @Transactional
    public List<IncomeResponseDto> getIncomesForUser(User user, String month) {
        List<Income> incomes;

        if (month == null || month.isBlank()) {
            incomes = incomeRepository.findByUser_Id(user.getId());
        } else {
            YearMonth yearMonth = YearMonth.parse(month);

            LocalDate start = yearMonth.atDay(1);
            LocalDate end = yearMonth.plusMonths(1).atDay(1);

            incomes = incomeRepository.findByUser_IdAndDateReceivedBetween(
                    user.getId(),
                    start,
                    end
            );
        }

        List<IncomeResponseDto> result = new ArrayList<>();

        for (Income income : incomes) {
            result.add(mapToDto(income));
        }

        return result;
    }

    @Transactional
    public IncomeResponseDto getById(User user, Integer id) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Income not found"));

        if (!income.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Income not found");
        }

        return mapToDto(income);
    }

    @Transactional
    public IncomeResponseDto create(User user, IncomeCreateRequestDto request) {
        Category category = getValidIncomeCategoryForUser(
                user,
                request.getCategoryId()
        );

        Income income = new Income();

        income.setUser(user);
        income.setCategory(category);
        income.setTitle(request.getTitle());
        income.setAmount(request.getAmount());
        income.setDateReceived(request.getDateReceived());
        income.setNotes(request.getNotes());
        income.setCreatedAt(LocalDateTime.now());

        Income savedIncome = incomeRepository.save(income);
        savingService.processSavings(savedIncome);

        return mapToDto(savedIncome);
    }

    @Transactional
    public IncomeResponseDto update(
            User user,
            Integer id,
            IncomeUpdateRequestDto request
    ) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Income not found"));

        if (!income.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Income not found");
        }

        Category category = getValidIncomeCategoryForUser(
                user,
                request.getCategoryId()
        );

        income.setCategory(category);
        income.setTitle(request.getTitle());
        income.setAmount(request.getAmount());
        income.setDateReceived(request.getDateReceived());
        income.setNotes(request.getNotes());

        Income savedIncome = incomeRepository.save(income);

        return mapToDto(savedIncome);
    }

    @Transactional
    public void delete(User user, Integer id) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Income not found"));

        if (!income.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Income not found");
        }

        incomeRepository.delete(income);
    }

    private Category getValidIncomeCategoryForUser(
            User user,
            Integer categoryId
    ) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Category not found");
        }

        if (category.getType() != CategoryType.INCOME) {
            throw new IllegalArgumentException("Category must be INCOME");
        }

        return category;
    }

    private IncomeResponseDto mapToDto(Income income) {
        IncomeResponseDto dto = new IncomeResponseDto();

        dto.setId(income.getId());
        dto.setTitle(income.getTitle());
        dto.setAmount(income.getAmount());
        dto.setDateReceived(income.getDateReceived());
        dto.setNotes(income.getNotes());
        dto.setCreatedAt(income.getCreatedAt());

        if (income.getCategory() != null) {
            dto.setCategoryId(income.getCategory().getId());
            dto.setCategoryName(income.getCategory().getName());
        }

        return dto;
    }
}