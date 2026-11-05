package com.finflow.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

@Schema(description = "Resumo analítico financeiro para os cartões e gráficos do Dashboard")
public record DashboardSummaryResponse(
    @Schema(description = "Total de receitas no período (centavos)", example = "750000")
    Long totalIncomeCents,

    @Schema(description = "Total de despesas no período (centavos)", example = "320000")
    Long totalExpenseCents,

    @Schema(description = "Saldo líquido do período (centavos)", example = "430000")
    Long netBalanceCents,

    @Schema(description = "Distribuição percentual de gastos por categoria")
    List<CategorySpendingItem> categorySpending
) implements Serializable {

    public record CategorySpendingItem(
        String categoryName,
        String colorHex,
        Long totalCents,
        Double percentage
    ) implements Serializable {}
}
