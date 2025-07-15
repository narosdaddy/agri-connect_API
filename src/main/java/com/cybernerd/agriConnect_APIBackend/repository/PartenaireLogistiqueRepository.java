package com.cybernerd.agriConnect_APIBackend.repository;

import com.cybernerd.agriConnect_APIBackend.model.PartenaireLogistique;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PartenaireLogistiqueRepository extends JpaRepository<PartenaireLogistique, UUID> {
    boolean existsByNom(String nom);
    boolean existsByEmail(String email);
} 