package com.dragos.lifebalance.repository;

import com.dragos.lifebalance.entity.SavingSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingSettingRepository extends JpaRepository<SavingSetting, Integer> {
}