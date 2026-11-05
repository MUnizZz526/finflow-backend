package com.finflow.application.service;

import com.finflow.application.dto.response.DashboardSummaryResponse;
import com.finflow.domain.model.enums.TransactionStatus;
import com.finflow.domain.model.enums.TransactionType;
import com.finflow.domain.repository.TransactionRepository;
import com.finflow.domain.repository.TransactionRepository.CategorySpendingProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - DashboardService")
class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Deve calcular resumo analítico com receitas, despesas, saldo líquido e percentuais de categoria")
    void shouldCalculateDashboardSummaryWithCorrectPercentages() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);

        when(transactionRepository.sumAmountByTypeAndPeriod(userId, TransactionType.INCOME, TransactionStatus.COMPLETED, start, end))
            .thenReturn(1000000L); // R$ 10.000,00

        when(transactionRepository.sumAmountByTypeAndPeriod(userId, TransactionType.EXPENSE, TransactionStatus.COMPLETED, start, end))
            .thenReturn(400000L); // R$ 4.000,00

        // Mock de projeção de categoria
        CategorySpendingProjection alimentacao = new CategorySpendingProjection() {
            @Override public String getCategoryName() { return "Alimentação"; }
            @Override public String getColorHex() { return "#EF4444"; }
            @Override public Long getTotalCents() { return 300000L; } // R$ 3.000,00 (75% de 4.000)
        };

        CategorySpendingProjection transporte = new CategorySpendingProjection() {
            @Override public String getCategoryName() { return "Transporte"; }
            @Override public String getColorHex() { return "#3B82F6"; }
            @Override public Long getTotalCents() { return 100000L; } // R$ 1.000,00 (25% de 4.000)
        };

        when(transactionRepository.findSpendingByCategoryAndPeriod(userId, start, end))
            .thenReturn(List.of(alimentacao, transporte));

        // Act
        DashboardSummaryResponse summary = dashboardService.getSummary(userId, start, end);

        // Assert
        assertThat(summary).isNotNull();
        assertThat(summary.totalIncomeCents()).isEqualTo(1000000L);
        assertThat(summary.totalExpenseCents()).isEqualTo(400000L);
        assertThat(summary.netBalanceCents()).isEqualTo(600000L); // 10.000 - 4.000 = 6.000

        assertThat(summary.categorySpending()).hasSize(2);
        assertThat(summary.categorySpending().get(0).categoryName()).isEqualTo("Alimentação");
        assertThat(summary.categorySpending().get(0).percentage()).isEqualTo(75.0);

        assertThat(summary.categorySpending().get(1).categoryName()).isEqualTo("Transporte");
        assertThat(summary.categorySpending().get(1).percentage()).isEqualTo(25.0);
    }
}
