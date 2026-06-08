package com.dragos.lifebalance.controller;

import com.dragos.lifebalance.dto.ExpenseCreateRequestDto;
import com.dragos.lifebalance.dto.ExpenseResponseDto;
import com.dragos.lifebalance.dto.ExpenseUpdateRequestDto;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponseDto>> getAll(
            Authentication authentication,
            @RequestParam(required = false) String month
    ) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                expenseService.getExpensesForUser(user, month)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDto> getById(
            Authentication authentication,
            @PathVariable Integer id
    ) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                expenseService.getById(user, id)
        );
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDto> create(
            Authentication authentication,
            @RequestBody @Valid ExpenseCreateRequestDto request
    ) {
        User user = (User) authentication.getPrincipal();

        ExpenseResponseDto response = expenseService.create(user, request);

        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponseDto> update(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestBody @Valid ExpenseUpdateRequestDto request
    ) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                expenseService.update(user, id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable Integer id
    ) {
        User user = (User) authentication.getPrincipal();

        expenseService.delete(user, id);

        return ResponseEntity.noContent().build();
    }
}