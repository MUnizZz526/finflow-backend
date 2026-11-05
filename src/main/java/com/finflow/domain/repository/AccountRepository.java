package com.finflow.domain.repository;

import com.finflow.domain.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findAllByUserIdAndIsActiveTrue(UUID userId);
    Optional<Account> findByIdAndUserId(UUID id, UUID userId);
}

