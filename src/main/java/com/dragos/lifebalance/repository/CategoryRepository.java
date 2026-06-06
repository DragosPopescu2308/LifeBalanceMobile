package com.dragos.lifebalance.repository;

import com.dragos.lifebalance.entity.Category;
import com.dragos.lifebalance.entity.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findByUser_Id(Integer userId);

    List<Category> findByUser_IdAndType(Integer userId, CategoryType type);
}