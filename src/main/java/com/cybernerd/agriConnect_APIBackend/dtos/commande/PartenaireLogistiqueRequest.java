package com.cybernerd.agriConnect_APIBackend.dtos.commande;

import lombok.Data;

@Data
public class PartenaireLogistiqueRequest {
    private String nom;
    private String description;
    private String email;
    private String telephone;
} 