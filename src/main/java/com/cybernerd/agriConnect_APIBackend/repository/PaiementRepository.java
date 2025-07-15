package com.cybernerd.agriConnect_APIBackend.repository;

import com.cybernerd.agriConnect_APIBackend.model.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PaiementRepository extends JpaRepository<Paiement, UUID> {
} 