package com.dragos.lifebalance.controller;

import com.dragos.lifebalance.dto.DashboardResponseDto;
import com.dragos.lifebalance.dto.DashboardTrendPointDto;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponseDto> monthly(
            Authentication authentication,
            @RequestParam(required = false) String month
    ) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                dashboardService.getMonthlyDashboard(user, month)
        );
    }

    @GetMapping("/trend")
    public ResponseEntity<List<DashboardTrendPointDto>> trend(
            Authentication authentication,
            @RequestParam(defaultValue = "6") int months,
            @RequestParam(required = false) String endMonth
    ) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                dashboardService.getTrend(user, months, endMonth)
        );
    }
}