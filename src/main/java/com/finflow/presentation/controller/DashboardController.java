package com.finflow.presentation.controller;

import com.finflow.application.dto.response.DashboardSummaryResponse;
import com.finflow.application.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints de métricas consolidadas e gráficos analíticos")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Obter resumo analítico para os cards e gráficos", description = "Retorna receitas, despesas, saldo líquido e distribuição por categoria com cache Redis")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
        @RequestHeader(name = "X-User-Id") UUID userId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        DashboardSummaryResponse summary = dashboardService.getSummary(userId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }
}

