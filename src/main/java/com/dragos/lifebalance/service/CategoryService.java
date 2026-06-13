package com.dragos.lifebalance.service;


import com.dragos.lifebalance.dto.CategoryCreateRequestDto;
import com.dragos.lifebalance.dto.CategoryResponseDto;
import com.dragos.lifebalance.dto.CategoryUpdateDto;
import com.dragos.lifebalance.entity.Category;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.entity.enums.CategoryType;
import com.dragos.lifebalance.exceptions.NotFoundException;
import com.dragos.lifebalance.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }


        public List<CategoryResponseDto> getForUser(User user, CategoryType type) {

            List<Category> categories;

            if (type == null) {
                categories = categoryRepository.findByUser_Id(user.getId());
            } else {
                categories = categoryRepository.findByUser_IdAndType(
                        user.getId(),
                        type
                );
            }

            List<CategoryResponseDto> result = new ArrayList<>();

            for (Category category : categories) {
                result.add(mapToDto(category));
            }

            return result;
        }

    @Transactional
    public CategoryResponseDto create(User user, CategoryCreateRequestDto dto) {
        Category category = new Category();

        category.setUser(user);
        category.setName(dto.getName());
        category.setType(dto.getType());
        category.setCreatedAt(LocalDateTime.now());

        Category savedCategory = categoryRepository.save(category);

        return mapToDto(savedCategory);
    }

    @Transactional
    public CategoryResponseDto update(User user, Integer id, CategoryUpdateDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Category not found");
        }

        category.setName(dto.getName());
        category.setType(dto.getType());

        Category savedCategory = categoryRepository.save(category);

        return mapToDto(savedCategory);
    }

    @Transactional
    public void delete(User user, Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Category not found");
        }

        categoryRepository.delete(category);
    }


    private CategoryResponseDto mapToDto(Category category) {
        CategoryResponseDto dto = new CategoryResponseDto();

        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setType(category.getType());
        dto.setCreatedAt(category.getCreatedAt());

        return dto;
    }

}
