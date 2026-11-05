package com.finflow.domain.repository;

import com.finflow.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    
    // Retorna categorias personalizadas do usuário + categorias globais do sistema (user_id IS NULL)
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.user IS NULL ORDER BY c.name ASC")
    List<Category> findAllAvailableForUser(@Param("userId") UUID userId);

    Optional<Category> findByIdAndUserId(UUID id, UUID userId);
}

