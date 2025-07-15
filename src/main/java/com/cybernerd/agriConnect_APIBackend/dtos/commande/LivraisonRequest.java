package com.cybernerd.agriConnect_APIBackend.dtos.commande;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LivraisonRequest {
    private UUID commandeId;
    private UUID partenaireLogistiqueId;
    private LocalDateTime dateLivraisonPrevue;
    private String informationsSuivi;
    private Double cout;
} 