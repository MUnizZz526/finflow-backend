package com.finflow.presentation.controller;

import com.finflow.application.dto.request.CreateTransactionRequest;
import com.finflow.application.dto.request.CreateTransferRequest;
import com.finflow.application.dto.response.TransactionResponse;
import com.finflow.application.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transações", description = "Endpoints para gerenciamento de receitas, despesas e transferências")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Criar nova receita ou despesa", description = "Cria um lançamento com suporte a chave de idempotência para evitar duplicidade")
    public ResponseEntity<TransactionResponse> createTransaction(
        @RequestHeader(name = "X-User-Id") UUID userId,
        @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
        @Valid @RequestBody CreateTransactionRequest request
    ) {
        TransactionResponse response = transactionService.createTransaction(userId, request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/transfers")
    @Operation(summary = "Realizar transferência entre contas", description = "Executa débito e crédito atômicos entre duas contas do usuário")
    public ResponseEntity<TransactionResponse> transfer(
        @RequestHeader(name = "X-User-Id") UUID userId,
        @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
        @Valid @RequestBody CreateTransferRequest request
    ) {
        TransactionResponse response = transactionService.transfer(userId, request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar transações por período", description = "Retorna histórico paginado de transações filtradas por data")
    public ResponseEntity<Page<TransactionResponse>> listByPeriod(
        @RequestHeader(name = "X-User-Id") UUID userId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<TransactionResponse> response = transactionService.listByPeriod(userId, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }
}

