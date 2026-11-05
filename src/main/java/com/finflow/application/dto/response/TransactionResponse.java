package com.finflow.application.dto.response;

import com.finflow.domain.model.Transaction;
import com.finflow.domain.model.enums.TransactionStatus;
import com.finflow.domain.model.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Representação detalhada de uma transação financeira")
public record TransactionResponse(
    UUID id,
    UUID accountId,
    String accountName,
    UUID categoryId,
    String categoryName,
    TransactionType type,
    Long amountCents,
    LocalDate transactionDate,
    String description,
    TransactionStatus status,
    UUID transferTargetAccountId,
    Instant createdAt
) {
    public static TransactionResponse fromEntity(Transaction t) {
        return new TransactionResponse(
            t.getId(),
            t.getAccount().getId(),
            t.getAccount().getName(),
            t.getCategory() != null ? t.getCategory().getId() : null,
            t.getCategory() != null ? t.getCategory().getName() : null,
            t.getType(),
            t.getAmountCents(),
            t.getTransactionDate(),
            t.getDescription(),
            t.getStatus(),
            t.getTransferTargetAccount() != null ? t.getTransferTargetAccount().getId() : null,
            t.getCreatedAt()
        );
    }
}
