package com.yowyob.dev.controllers;

import com.yowyob.dev.dto.responseDTO.DashboardStatsDTO;
import com.yowyob.dev.services.DashboardService;
import com.yowyob.dev.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/statistics")
    @Operation(summary = "Obtenir les statistiques pour le tableau de bord")
    public Mono<DashboardStatsDTO> getStatistics() {
        return JwtUtils.getCurrentUserInfo().flatMap(userInfo -> {
            // Supposons que l'userId du token est l'agencyId pour la création
            UUID agencyId = UUID.fromString(userInfo.getUserId());
            return dashboardService.getDashboardStatistics(userInfo.getUsername(), agencyId);
        });
    }
}