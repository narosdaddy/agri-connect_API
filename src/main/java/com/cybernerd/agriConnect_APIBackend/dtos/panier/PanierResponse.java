package com.cybernerd.agriConnect_APIBackend.dtos.panier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PanierResponse {

    private UUID id;
    private List<ElementPanierResponse> elements;
    private String codePromo;
    private BigDecimal sousTotal;
    private BigDecimal fraisLivraison;
    private BigDecimal remise;
    private BigDecimal total;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private int nombreElements;
    private boolean vide;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ElementPanierResponse {
        private UUID id;
        private UUID produitId;
        private String nomProduit;
        private String imagePrincipale;
        private BigDecimal prixUnitaire;
        private Integer quantite;
        private BigDecimal prixTotal;
        private String unite;
        private boolean disponible;
    }
} 