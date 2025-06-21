package com.cybernerd.agriConnect_APIBackend.repository;

import com.cybernerd.agriConnect_APIBackend.model.Panier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PanierRepository extends JpaRepository<Panier, UUID> {

    Optional<Panier> findByAcheteurId(UUID acheteurId);

    boolean existsByAcheteurId(UUID acheteurId);

    void deleteByAcheteurId(UUID acheteurId);
} 