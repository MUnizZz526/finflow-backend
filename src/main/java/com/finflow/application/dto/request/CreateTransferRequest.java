package com.finflow.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Requisição para transferência atômica entre duas contas")
public record CreateTransferRequest(

    @NotNull(message = "A conta de origem é obrigatória")
    @Schema(description = "Identificador da conta que sairá o dinheiro")
    UUID sourceAccountId,

    @NotNull(message = "A conta de destino é obrigatória")
    @Schema(description = "Identificador da conta que receberá o dinheiro")
    UUID targetAccountId,

    @NotNull(message = "O valor em centavos é obrigatório")
    @Positive(message = "O valor deve ser estritamente positivo")
    @Schema(description = "Valor transferido em centavos", example = "15000")
    Long amountCents,

    @NotNull(message = "A data da transferência é obrigatória")
    @Schema(description = "Data da transferência", example = "2026-11-05")
    LocalDate transactionDate,

    @NotBlank(message = "A descrição não pode ser vazia")
    @Size(max = 255)
    @Schema(description = "Descrição da transferência", example = "Transferência para Reserva de Emergência")
    String description
) {}

