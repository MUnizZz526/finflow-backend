package com.finflow.application.service;

import com.finflow.application.dto.response.DashboardSummaryResponse;
import com.finflow.application.dto.response.DashboardSummaryResponse.CategorySpendingItem;
import com.finflow.domain.model.enums.TransactionStatus;
import com.finflow.domain.model.enums.TransactionType;
import com.finflow.domain.repository.TransactionRepository;
import com.finflow.domain.repository.TransactionRepository.CategorySpendingProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;

    /**
     * Calcula o resumo analítico da dashboard com cache no Redis.
     * Consultas agregadas pesadas não recalculam se os dados não foram alterados.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboard-summary", key = "#userId.toString() + '-' + #startDate.toString() + '-' + #endDate.toString()")
    public DashboardSummaryResponse getSummary(UUID userId, LocalDate startDate, LocalDate endDate) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        Long totalIncome = transactionRepository.sumAmountByTypeAndPeriod(
            userId, TransactionType.INCOME, TransactionStatus.COMPLETED, start, end
        );

        Long totalExpense = transactionRepository.sumAmountByTypeAndPeriod(
            userId, TransactionType.EXPENSE, TransactionStatus.COMPLETED, start, end
        );

        Long netBalance = totalIncome - totalExpense;

        List<CategorySpendingProjection> projections = transactionRepository.findSpendingByCategoryAndPeriod(
            userId, start, end
        );

        List<CategorySpendingItem> categoryItems = projections.stream()
            .map(p -> {
                double percentage = totalExpense > 0 
                    ? (p.getTotalCents().doubleValue() / totalExpense.doubleValue()) * 100.0 
                    : 0.0;
                return new CategorySpendingItem(
                    p.getCategoryName() != null ? p.getCategoryName() : "Outros",
                    p.getColorHex() != null ? p.getColorHex() : "#6B7280",
                    p.getTotalCents(),
                    Math.round(percentage * 100.0) / 100.0 // Arredonda para 2 casas decimais
                );
            })
            .toList();

        return new DashboardSummaryResponse(totalIncome, totalExpense, netBalance, categoryItems);
    }
}
