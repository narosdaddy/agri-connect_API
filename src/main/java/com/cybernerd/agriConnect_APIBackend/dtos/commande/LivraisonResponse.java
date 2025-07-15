package com.cybernerd.agriConnect_APIBackend.dtos.commande;

import com.cybernerd.agriConnect_APIBackend.enumType.StatutLivraison;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LivraisonResponse {
    private UUID id;
    private UUID commandeId;
    private UUID partenaireLogistiqueId;
    private StatutLivraison statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateLivraisonPrevue;
    private LocalDateTime dateLivraisonEffective;
    private String informationsSuivi;
    private Double cout;
} 