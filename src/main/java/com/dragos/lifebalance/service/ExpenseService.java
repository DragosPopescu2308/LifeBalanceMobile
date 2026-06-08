package com.dragos.lifebalance.service;

import com.dragos.lifebalance.dto.ExpenseCreateRequestDto;
import com.dragos.lifebalance.dto.ExpenseResponseDto;
import com.dragos.lifebalance.dto.ExpenseUpdateRequestDto;
import com.dragos.lifebalance.entity.Category;
import com.dragos.lifebalance.entity.Expense;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.entity.enums.CategoryType;
import com.dragos.lifebalance.repository.CategoryRepository;
import com.dragos.lifebalance.repository.ExpenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            CategoryRepository categoryRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ExpenseResponseDto> getExpensesForUser(User user, String month) {
        List<Expense> expenses;

        if (month == null || month.isBlank()) {
            expenses = expenseRepository.findByUser_Id(user.getId());
        } else {
            YearMonth yearMonth = YearMonth.parse(month);

            LocalDate start = yearMonth.atDay(1);
            LocalDate end = yearMonth.plusMonths(1).atDay(1);

            expenses = expenseRepository.findByUser_IdAndDateSpentBetween(
                    user.getId(),
                    start,
                    end
            );
        }

        List<ExpenseResponseDto> result = new ArrayList<>();

        for (Expense expense : expenses) {
            result.add(mapToDto(expense));
        }

        return result;
    }

    public ExpenseResponseDto getById(User user, Integer id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (expense.getUser().getId() != user.getId()) {
            throw new RuntimeException("Expense not found");
        }

        return mapToDto(expense);
    }

    @Transactional
    public ExpenseResponseDto create(User user, ExpenseCreateRequestDto request) {
        Category category = getValidExpenseCategoryForUser(
                user,
                request.getCategoryId()
        );

        Expense expense = new Expense();

        expense.setUser(user);
        expense.setCategory(category);
        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setDateSpent(request.getDateSpent());
        expense.setNotes(request.getNotes());
        expense.setCreatedAt(LocalDateTime.now());

        Expense savedExpense = expenseRepository.save(expense);

        return mapToDto(savedExpense);
    }

    @Transactional
    public ExpenseResponseDto update(
            User user,
            Integer id,
            ExpenseUpdateRequestDto request
    ) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (expense.getUser().getId() != user.getId()) {
            throw new RuntimeException("Expense not found");
        }

        Category category = getValidExpenseCategoryForUser(
                user,
                request.getCategoryId()
        );

        expense.setCategory(category);
        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setDateSpent(request.getDateSpent());
        expense.setNotes(request.getNotes());

        Expense savedExpense = expenseRepository.save(expense);

        return mapToDto(savedExpense);
    }

    @Transactional
    public void delete(User user, Integer id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (expense.getUser().getId() != user.getId()) {
            throw new RuntimeException("Expense not found");
        }

        expenseRepository.delete(expense);
    }

    private Category getValidExpenseCategoryForUser(
            User user,
            Integer categoryId
    ) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (category.getUser().getId() != user.getId()) {
            throw new RuntimeException("Category not found");
        }

        if (category.getType() != CategoryType.EXPENSE) {
            throw new RuntimeException("Category must be EXPENSE");
        }

        return category;
    }

    private ExpenseResponseDto mapToDto(Expense expense) {
        ExpenseResponseDto dto = new ExpenseResponseDto();

        dto.setId(expense.getId());
        dto.setTitle(expense.getTitle());
        dto.setAmount(expense.getAmount());
        dto.setDateSpent(expense.getDateSpent());
        dto.setNotes(expense.getNotes());
        dto.setCreatedAt(expense.getCreatedAt());

        if (expense.getCategory() != null) {
            dto.setCategoryId(expense.getCategory().getId());
            dto.setCategoryName(expense.getCategory().getName());
        }

        return dto;
    }
}