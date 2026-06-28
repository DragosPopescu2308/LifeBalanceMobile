package com.dragos.lifebalance.controller;

import com.dragos.lifebalance.dto.SavingSettingDto;
import com.dragos.lifebalance.dto.SavingSummaryDto;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.service.SavingService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/savings")
public class SavingController {

    private final SavingService savingService;

    public SavingController(SavingService savingService) {
        this.savingService = savingService;
    }

    @GetMapping("/settings")
    public SavingSettingDto getSettings(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return savingService.getSettings(user);
    }

    @PutMapping("/settings")
    public SavingSettingDto updateSettings(
            Authentication authentication,
            @RequestBody @Valid SavingSettingDto request
    ) {
        User user = (User) authentication.getPrincipal();
        return savingService.updateSettings(user, request);
    }

    @GetMapping("/monthly")
    public BigDecimal monthlyTotal(
            Authentication authentication,
            @RequestParam String month
    ) {
        User user = (User) authentication.getPrincipal();
        return savingService.getMonthlyTotal(user, month);
    }

    @GetMapping("/remaining")
    public BigDecimal remaining(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return savingService.getRemainingSavings(user);
    }

    @GetMapping("/summary")
    public SavingSummaryDto summary(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return savingService.getSummary(user);
    }
}