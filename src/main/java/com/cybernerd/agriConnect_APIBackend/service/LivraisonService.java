package com.cybernerd.agriConnect_APIBackend.service;

import com.cybernerd.agriConnect_APIBackend.dtos.commande.LivraisonRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.commande.LivraisonResponse;
import java.util.List;
import java.util.UUID;

public interface LivraisonService {
    LivraisonResponse creerLivraison(LivraisonRequest request);
    LivraisonResponse getLivraisonById(UUID livraisonId);
    List<LivraisonResponse> getLivraisonsByCommande(UUID commandeId);
    List<LivraisonResponse> getLivraisonsByPartenaire(UUID partenaireId);
    LivraisonResponse mettreAJourStatut(UUID livraisonId, String nouveauStatut);
    void supprimerLivraison(UUID livraisonId);
} 