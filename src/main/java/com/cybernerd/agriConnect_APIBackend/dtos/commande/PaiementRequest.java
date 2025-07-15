package com.cybernerd.agriConnect_APIBackend.dtos.commande;

import com.cybernerd.agriConnect_APIBackend.enumType.MethodePaiement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaiementRequest {
    private float montant;
    private MethodePaiement methode;
    private String statut;
    private UUID commandeId;
} 