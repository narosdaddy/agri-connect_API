package com.cybernerd.agriConnect_APIBackend.dtos.commande;

import com.cybernerd.agriConnect_APIBackend.enumType.StatutPartenaire;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PartenaireLogistiqueResponse {
    private UUID id;
    private String nom;
    private String description;
    private String email;
    private String telephone;
    private StatutPartenaire statut;
    private LocalDateTime dateInscription;
} 