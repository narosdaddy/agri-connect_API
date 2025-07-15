package com.cybernerd.agriConnect_APIBackend.repository;

import com.cybernerd.agriConnect_APIBackend.model.CategorieProduit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategorieProduitRepository extends JpaRepository<CategorieProduit, UUID> {
    Optional<CategorieProduit> findByNom(String nom);
} 