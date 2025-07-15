package com.cybernerd.agriConnect_APIBackend.repository;

import com.cybernerd.agriConnect_APIBackend.model.Livraison;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LivraisonRepository extends JpaRepository<Livraison, UUID> {
    List<Livraison> findByCommandeId(UUID commandeId);
    List<Livraison> findByPartenaireLogistiqueId(UUID partenaireId);
} 