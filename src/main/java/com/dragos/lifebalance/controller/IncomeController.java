package com.dragos.lifebalance.controller;

import com.dragos.lifebalance.dto.IncomeCreateRequestDto;
import com.dragos.lifebalance.dto.IncomeResponseDto;
import com.dragos.lifebalance.dto.IncomeUpdateRequestDto;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.service.IncomeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GetMapping
    public ResponseEntity<List<IncomeResponseDto>> getAll(
            Authentication authentication,
            @RequestParam(required = false) String month
    ) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                incomeService.getIncomesForUser(user, month)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponseDto> getById(
            Authentication authentication,
            @PathVariable Integer id
    ) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                incomeService.getById(user, id)
        );
    }

    @PostMapping
    public ResponseEntity<IncomeResponseDto> create(
            Authentication authentication,
            @RequestBody @Valid IncomeCreateRequestDto request
    ) {
        User user = (User) authentication.getPrincipal();

        IncomeResponseDto response = incomeService.create(user, request);

        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponseDto> update(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestBody @Valid IncomeUpdateRequestDto request
    ) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                incomeService.update(user, id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable Integer id
    ) {
        User user = (User) authentication.getPrincipal();

        incomeService.delete(user, id);

        return ResponseEntity.noContent().build();
    }
}