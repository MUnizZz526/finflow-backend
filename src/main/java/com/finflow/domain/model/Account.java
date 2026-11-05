package com.finflow.domain.model;

import com.finflow.domain.model.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountType type;

    @Column(name = "balance_cents", nullable = false)
    @Builder.Default
    private Long balanceCents = 0L;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "BRL";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Métodos de Negócio Ricos (Domain-Driven Design)
    public void credit(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("O valor de crédito deve ser positivo.");
        }
        this.balanceCents += amount;
    }

    public void debit(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("O valor de débito deve ser positivo.");
        }
        this.balanceCents -= amount;
    }
}

