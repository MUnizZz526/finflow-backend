package com.finflow.application.service;

import com.finflow.application.dto.request.CreateTransactionRequest;
import com.finflow.application.dto.request.CreateTransferRequest;
import com.finflow.application.dto.response.TransactionResponse;
import com.finflow.application.exception.BusinessException;
import com.finflow.application.exception.DuplicateTransactionException;
import com.finflow.application.exception.ResourceNotFoundException;
import com.finflow.domain.model.Account;
import com.finflow.domain.model.Category;
import com.finflow.domain.model.Transaction;
import com.finflow.domain.model.User;
import com.finflow.domain.model.enums.TransactionStatus;
import com.finflow.domain.model.enums.TransactionType;
import com.finflow.domain.repository.AccountRepository;
import com.finflow.domain.repository.CategoryRepository;
import com.finflow.domain.repository.TransactionRepository;
import com.finflow.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * Criação de Receitas ou Despesas com validação de Idempotência.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = "dashboard-summary", key = "#userId", allEntries = true)
    public TransactionResponse createTransaction(UUID userId, CreateTransactionRequest request, String idempotencyKey) {
        // 1. Verificação de Idempotência
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Transaction> existing = transactionRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
            if (existing.isPresent()) {
                return TransactionResponse.fromEntity(existing.get());
            }
        }

        if (request.type() == TransactionType.TRANSFER) {
            throw new BusinessException("Para transferências, utilize o endpoint específico de transferência.");
        }

        User user = getUserOrThrow(userId);
        Account account = getAccountOrThrow(request.accountId(), userId);

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                .filter(c -> c.getUser() == null || c.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada ou não pertence ao usuário."));
        }

        // 2. Aplicação das Regras de Domínio no Saldo da Conta
        if (request.type() == TransactionType.INCOME) {
            account.credit(request.amountCents());
        } else if (request.type() == TransactionType.EXPENSE) {
            account.debit(request.amountCents());
        }

        // 3. Persistência da Transação
        Transaction transaction = Transaction.builder()
            .user(user)
            .account(account)
            .category(category)
            .type(request.type())
            .amountCents(request.amountCents())
            .transactionDate(request.transactionDate())
            .description(request.description())
            .status(TransactionStatus.COMPLETED)
            .idempotencyKey(idempotencyKey)
            .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        accountRepository.save(account);

        return TransactionResponse.fromEntity(savedTransaction);
    }

    /**
     * Transferência Atômica entre duas contas (ACID).
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = "dashboard-summary", key = "#userId", allEntries = true)
    public TransactionResponse transfer(UUID userId, CreateTransferRequest request, String idempotencyKey) {
        if (request.sourceAccountId().equals(request.targetAccountId())) {
            throw new BusinessException("A conta de origem e destino devem ser diferentes.");
        }

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Transaction> existing = transactionRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
            if (existing.isPresent()) {
                return TransactionResponse.fromEntity(existing.get());
            }
        }

        User user = getUserOrThrow(userId);
        Account sourceAccount = getAccountOrThrow(request.sourceAccountId(), userId);
        Account targetAccount = getAccountOrThrow(request.targetAccountId(), userId);

        // Operação Atômica: debita de uma e credita na outra
        sourceAccount.debit(request.amountCents());
        targetAccount.credit(request.amountCents());

        Transaction transferTransaction = Transaction.builder()
            .user(user)
            .account(sourceAccount)
            .transferTargetAccount(targetAccount)
            .type(TransactionType.TRANSFER)
            .amountCents(request.amountCents())
            .transactionDate(request.transactionDate())
            .description(request.description())
            .status(TransactionStatus.COMPLETED)
            .idempotencyKey(idempotencyKey)
            .build();

        Transaction saved = transactionRepository.save(transferTransaction);
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        return TransactionResponse.fromEntity(saved);
    }

    /**
     * Listagem paginada por período.
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> listByPeriod(UUID userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        return transactionRepository.findByPeriod(userId, start, end, pageable)
            .map(TransactionResponse::fromEntity);
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    private Account getAccountOrThrow(UUID accountId, UUID userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada ou não pertence ao usuário."));
    }
}
