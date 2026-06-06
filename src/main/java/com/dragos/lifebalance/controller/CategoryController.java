package com.dragos.lifebalance.controller;

import com.dragos.lifebalance.dto.CategoryCreateRequestDto;
import com.dragos.lifebalance.dto.CategoryResponseDto;
import com.dragos.lifebalance.dto.CategoryUpdateDto;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.entity.enums.CategoryType;
import com.dragos.lifebalance.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllForUser(
            Authentication authentication,
            @RequestParam(required = false) CategoryType type
    ) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                categoryService.getForUser(user, type)
        );
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> create(
            Authentication authentication,
            @RequestBody @Valid CategoryCreateRequestDto request
    ) {
        User user = (User) authentication.getPrincipal();

        CategoryResponseDto dto = categoryService.create(user, request);

        return ResponseEntity.status(201).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> update(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestBody @Valid CategoryUpdateDto request
    ) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                categoryService.update(user, id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable Integer id
    ) {
        User user = (User) authentication.getPrincipal();

        categoryService.delete(user, id);

        return ResponseEntity.noContent().build();
    }
}