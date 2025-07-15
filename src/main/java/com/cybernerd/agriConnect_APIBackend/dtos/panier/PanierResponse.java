package com.cybernerd.agriConnect_APIBackend.dtos.panier;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Réponse représentant le panier d'un utilisateur", example = "{\n  \"id\": \"123e4567-e89b-12d3-a456-426614174000\",\n  \"elements\": [\n    {\n      \"id\": \"123e4567-e89b-12d3-a456-426614174001\",\n      \"produitId\": \"123e4567-e89b-12d3-a456-426614174000\",\n      \"nomProduit\": \"Tomates cerises bio\",\n      \"imagePrincipale\": \"https://example.com/images/tomates.jpg\",\n      \"prixUnitaire\": 4.5,\n      \"quantite\": 2,\n      \"prixTotal\": 9.0,\n      \"unite\": \"kg\",\n      \"disponible\": true\n    }\n  ],\n  \"codePromo\": \"PROMO10\",\n  \"sousTotal\": 25.0,\n  \"fraisLivraison\": 5.0,\n  \"remise\": 2.0,\n  \"total\": 28.0,\n  \"dateCreation\": \"2024-06-25T12:00:00\",\n  \"dateModification\": \"2024-06-25T12:30:00\",\n  \"nombreElements\": 1,\n  \"vide\": false\n}")
public class PanierResponse {

    @Schema(description = "ID du panier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;
    @Schema(description = "Liste des éléments du panier")
    private List<ElementPanierResponse> elements;
    @Schema(description = "Code promo appliqué", example = "PROMO10")
    private String codePromo;
    @Schema(description = "Sous-total du panier", example = "25.00")
    private BigDecimal sousTotal;
    @Schema(description = "Frais de livraison", example = "5.00")
    private BigDecimal fraisLivraison;
    @Schema(description = "Montant de la remise", example = "2.00")
    private BigDecimal remise;
    @Schema(description = "Total à payer", example = "28.00")
    private BigDecimal total;
    @Schema(description = "Date de création du panier")
    private LocalDateTime dateCreation;
    @Schema(description = "Date de dernière modification du panier")
    private LocalDateTime dateModification;
    @Schema(description = "Nombre d'éléments dans le panier", example = "3")
    private int nombreElements;
    @Schema(description = "Indique si le panier est vide", example = "false")
    private boolean vide;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Élément du panier dans la réponse")
    public static class ElementPanierResponse {
        @Schema(description = "ID de l'élément du panier", example = "123e4567-e89b-12d3-a456-426614174001")
        private UUID id;
        @Schema(description = "ID du produit", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID produitId;
        @Schema(description = "Nom du produit", example = "Tomates cerises bio")
        private String nomProduit;
        @Schema(description = "URL de l'image principale du produit", example = "https://example.com/images/tomates.jpg")
        private String imagePrincipale;
        @Schema(description = "Prix unitaire du produit", example = "4.50")
        private BigDecimal prixUnitaire;
        @Schema(description = "Quantité de ce produit dans le panier", example = "2")
        private Integer quantite;
        @Schema(description = "Prix total pour cet élément", example = "9.00")
        private BigDecimal prixTotal;
        @Schema(description = "Unité de mesure du produit", example = "kg")
        private String unite;
        @Schema(description = "Produit disponible ou non", example = "true")
        private boolean disponible;
    }
} 