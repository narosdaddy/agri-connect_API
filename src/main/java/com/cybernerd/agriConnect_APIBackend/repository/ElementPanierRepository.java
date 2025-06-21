package com.cybernerd.agriConnect_APIBackend.repository;

import com.cybernerd.agriConnect_APIBackend.model.ElementPanier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ElementPanierRepository extends JpaRepository<ElementPanier, UUID> {

    Optional<ElementPanier> findByPanierIdAndProduitId(UUID panierId, UUID produitId);

    void deleteByPanierId(UUID panierId);
} 