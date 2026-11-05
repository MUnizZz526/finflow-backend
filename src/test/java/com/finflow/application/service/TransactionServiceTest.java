package com.finflow.application.service;

import com.finflow.application.dto.request.CreateTransactionRequest;
import com.finflow.application.dto.request.CreateTransferRequest;
import com.finflow.application.dto.response.TransactionResponse;
import com.finflow.application.exception.BusinessException;
import com.finflow.application.exception.ResourceNotFoundException;
import com.finflow.domain.model.Account;
import com.finflow.domain.model.Category;
import com.finflow.domain.model.Transaction;
import com.finflow.domain.model.User;
import com.finflow.domain.model.enums.AccountType;
import com.finflow.domain.model.enums.TransactionType;
import com.finflow.domain.repository.AccountRepository;
import com.finflow.domain.repository.CategoryRepository;
import com.finflow.domain.repository.TransactionRepository;
import com.finflow.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - TransactionService")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User sampleUser;
    private Account sampleAccount;
    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        sampleUser = User.builder()
            .id(userId)
            .name("Cauã Muniz")
            .email("munizcaua88@gmail.com")
            .build();

        sampleAccount = Account.builder()
            .id(accountId)
            .user(sampleUser)
            .name("Conta Corrente Principal")
            .type(AccountType.CHECKING)
            .balanceCents(100000L) // R$ 1.000,00
            .build();
    }

    @Test
    @DisplayName("Deve criar transação de receita e creditar saldo da conta com sucesso")
    void shouldCreateIncomeTransactionAndCreditAccountSuccessfully() {
        // Arrange
        CreateTransactionRequest request = new CreateTransactionRequest(
            accountId, null, TransactionType.INCOME, 50000L, LocalDate.now(), "Salário Mensal"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(sampleAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        // Act
        TransactionResponse response = transactionService.createTransaction(userId, request, null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.amountCents()).isEqualTo(50000L);
        assertThat(response.type()).isEqualTo(TransactionType.INCOME);
        assertThat(sampleAccount.getBalanceCents()).isEqualTo(150000L); // 100000 + 50000

        verify(accountRepository).save(sampleAccount);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Deve criar transação de despesa e debitar saldo da conta com sucesso")
    void shouldCreateExpenseTransactionAndDebitAccountSuccessfully() {
        // Arrange
        CreateTransactionRequest request = new CreateTransactionRequest(
            accountId, null, TransactionType.EXPENSE, 30000L, LocalDate.now(), "Mercado"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(sampleAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        // Act
        TransactionResponse response = transactionService.createTransaction(userId, request, null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(sampleAccount.getBalanceCents()).isEqualTo(70000L); // 100000 - 30000
        verify(accountRepository).save(sampleAccount);
    }

    @Test
    @DisplayName("Deve retornar transação existente quando a chave de idempotência já existir (evita duplicidade)")
    void shouldReturnExistingTransactionWhenIdempotencyKeyMatches() {
        // Arrange
        String idempotencyKey = "uuid-requisicao-repetida-123";
        Transaction existingTransaction = Transaction.builder()
            .id(UUID.randomUUID())
            .user(sampleUser)
            .account(sampleAccount)
            .type(TransactionType.INCOME)
            .amountCents(50000L)
            .transactionDate(LocalDate.now())
            .description("Salário já lançado")
            .idempotencyKey(idempotencyKey)
            .build();

        when(transactionRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey))
            .thenReturn(Optional.of(existingTransaction));

        CreateTransactionRequest request = new CreateTransactionRequest(
            accountId, null, TransactionType.INCOME, 50000L, LocalDate.now(), "Salário"
        );

        // Act
        TransactionResponse response = transactionService.createTransaction(userId, request, idempotencyKey);

        // Assert
        assertThat(response.id()).isEqualTo(existingTransaction.getId());
        // Não deve persistir nada novo nem alterar saldo
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao tentar transferir entre a mesma conta")
    void shouldFailWhenTransferringBetweenSameAccounts() {
        // Arrange
        CreateTransferRequest request = new CreateTransferRequest(
            accountId, accountId, 20000L, LocalDate.now(), "Tentativa inválida"
        );

        // Act & Assert
        assertThatThrownBy(() -> transactionService.transfer(userId, request, null))
            .isInstanceOf(BusinessException.class)
            .hasMessage("A conta de origem e destino devem ser diferentes.");
    }

    @Test
    @DisplayName("Deve executar transferência atômica entre duas contas debitando e creditando corretamente")
    void shouldExecuteTransferBetweenDifferentAccountsAtomically() {
        // Arrange
        UUID targetAccountId = UUID.randomUUID();
        Account targetAccount = Account.builder()
            .id(targetAccountId)
            .user(sampleUser)
            .name("Conta Poupança / Reserva")
            .type(AccountType.SAVINGS)
            .balanceCents(20000L) // R$ 200,00
            .build();

        CreateTransferRequest request = new CreateTransferRequest(
            accountId, targetAccountId, 40000L, LocalDate.now(), "Aporte na reserva"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(sampleAccount));
        when(accountRepository.findByIdAndUserId(targetAccountId, userId)).thenReturn(Optional.of(targetAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        // Act
        TransactionResponse response = transactionService.transfer(userId, request, null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.type()).isEqualTo(TransactionType.TRANSFER);
        assertThat(sampleAccount.getBalanceCents()).isEqualTo(60000L); // 100000 - 40000
        assertThat(targetAccount.getBalanceCents()).isEqualTo(60000L); // 20000 + 40000

        verify(accountRepository).save(sampleAccount);
        verify(accountRepository).save(targetAccount);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException se a conta não pertencer ao usuário autenticado")
    void shouldFailWhenAccountDoesNotBelongToUser() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        CreateTransactionRequest request = new CreateTransactionRequest(
            accountId, null, TransactionType.INCOME, 1000L, LocalDate.now(), "Teste invasão"
        );

        // Act & Assert
        assertThatThrownBy(() -> transactionService.createTransaction(userId, request, null))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Conta não encontrada ou não pertence ao usuário.");
    }
}

