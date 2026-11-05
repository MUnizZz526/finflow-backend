package com.finflow.domain.repository;

import com.finflow.domain.model.Transaction;
import com.finflow.domain.model.enums.TransactionStatus;
import com.finflow.domain.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    // Listagem paginada e filtrada por período para extrato e dashboard
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.user.id = :userId
          AND t.transactionDate BETWEEN :startDate AND :endDate
        ORDER BY t.transactionDate DESC, t.createdAt DESC
    """)
    Page<Transaction> findByPeriod(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );

    // Agregação de totais por tipo (Receitas x Despesas) no período
    @Query("""
        SELECT COALESCE(SUM(t.amountCents), 0)
        FROM Transaction t
        WHERE t.user.id = :userId
          AND t.type = :type
          AND t.status = :status
          AND t.transactionDate BETWEEN :startDate AND :endDate
    """)
    Long sumAmountByTypeAndPeriod(
        @Param("userId") UUID userId,
        @Param("type") TransactionType type,
        @Param("status") TransactionStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Agregação de gastos por categoria (para gráfico de distribuição/pizza na dashboard)
    @Query("""
        SELECT c.name AS categoryName,
               c.colorHex AS colorHex,
               SUM(t.amountCents) AS totalCents
        FROM Transaction t
        LEFT JOIN t.category c
        WHERE t.user.id = :userId
          AND t.type = 'EXPENSE'
          AND t.status = 'COMPLETED'
          AND t.transactionDate BETWEEN :startDate AND :endDate
        GROUP BY c.id, c.name, c.colorHex
        ORDER BY totalCents DESC
    """)
    List<CategorySpendingProjection> findSpendingByCategoryAndPeriod(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    interface CategorySpendingProjection {
        String getCategoryName();
        String getColorHex();
        Long getTotalCents();
    }
}

