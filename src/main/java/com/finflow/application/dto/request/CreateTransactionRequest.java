package com.finflow.application.dto.request;

import com.finflow.domain.model.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Requisição para criação de uma transação de Receita ou Despesa")
public record CreateTransactionRequest(
    
    @NotNull(message = "O ID da conta é obrigatório")
    @Schema(description = "Identificador da conta", example = "a1b2c3d4-0000-0000-0000-000000000001")
    UUID accountId,

    @Schema(description = "Identificador da categoria (opcional)", example = "b2c3d4e5-0000-0000-0000-000000000002")
    UUID categoryId,

    @NotNull(message = "O tipo da transação é obrigatório (INCOME ou EXPENSE)")
    @Schema(description = "Tipo de lançamento financeiro", example = "EXPENSE")
    TransactionType type,

    @NotNull(message = "O valor em centavos é obrigatório")
    @Positive(message = "O valor deve ser estritamente positivo (maior que zero)")
    @Schema(description = "Valor expresso em centavos (ex: R$ 49,90 = 4990)", example = "4990")
    Long amountCents,

    @NotNull(message = "A data da transação é obrigatória")
    @Schema(description = "Data em que o lançamento ocorreu", example = "2026-11-05")
    LocalDate transactionDate,

    @NotBlank(message = "A descrição não pode ser vazia")
    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    @Schema(description = "Descrição detalhada do gasto ou ganho", example = "Supermercado Semanal")
    String description
) {}

