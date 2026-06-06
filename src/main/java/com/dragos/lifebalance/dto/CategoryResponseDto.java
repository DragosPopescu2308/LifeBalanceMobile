package com.dragos.lifebalance.dto;

import com.dragos.lifebalance.entity.enums.CategoryType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CategoryResponseDto {

    private Integer id;
    private String name;
    private CategoryType type;
    private LocalDateTime createdAt;
}