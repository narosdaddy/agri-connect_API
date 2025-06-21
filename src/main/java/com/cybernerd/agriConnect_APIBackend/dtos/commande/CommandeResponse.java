package com.cybernerd.agriConnect_APIBackend.dtos.commande;

import com.cybernerd.agriConnect_APIBackend.enumType.MethodePaiement;
import com.cybernerd.agriConnect_APIBackend.enumType.StatutCommande;
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
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse de commande")
public class CommandeResponse {

    @Schema(description = "ID de la commande")
    private UUID id;

    @Schema(description = "Numéro de commande", example = "CMD-2024-001")
    private String numeroCommande;

    @Schema(description = "ID de l'acheteur")
    private UUID acheteurId;

    @Schema(description = "Nom de l'acheteur")
    private String nomAcheteur;

    @Schema(description = "Email de l'acheteur")
    private String emailAcheteur;

    @Schema(description = "Éléments de la commande")
    private List<ElementCommandeResponse> elements;

    @Schema(description = "Statut de la commande")
    private StatutCommande statut;

    @Schema(description = "Méthode de paiement")
    private MethodePaiement methodePaiement;

    @Schema(description = "Sous-total")
    private BigDecimal sousTotal;

    @Schema(description = "Frais de livraison")
    private BigDecimal fraisLivraison;

    @Schema(description = "Remise")
    private BigDecimal remise;

    @Schema(description = "Total")
    private BigDecimal total;

    @Schema(description = "Adresse de livraison")
    private String adresseLivraison;

    @Schema(description = "Ville de livraison")
    private String villeLivraison;

    @Schema(description = "Code postal de livraison")
    private String codePostalLivraison;

    @Schema(description = "Pays de livraison")
    private String paysLivraison;

    @Schema(description = "Téléphone de livraison")
    private String telephoneLivraison;

    @Schema(description = "Instructions de livraison")
    private String instructionsLivraison;

    @Schema(description = "Code promo appliqué")
    private String codePromo;

    @Schema(description = "Date de création")
    private LocalDateTime dateCreation;

    @Schema(description = "Date de modification")
    private LocalDateTime dateModification;

    @Schema(description = "Date de livraison estimée")
    private LocalDateTime dateLivraisonEstimee;

    @Schema(description = "Date de livraison effective")
    private LocalDateTime dateLivraisonEffective;

    @Schema(description = "Nombre total d'éléments")
    private Integer nombreElements;

    @Schema(description = "Peut être annulée")
    private Boolean peutEtreAnnulee;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Élément de commande")
    public static class ElementCommandeResponse {

        @Schema(description = "ID de l'élément")
        private UUID id;

        @Schema(description = "ID du produit")
        private UUID produitId;

        @Schema(description = "Nom du produit")
        private String nomProduit;

        @Schema(description = "Image principale du produit")
        private String imagePrincipale;

        @Schema(description = "Prix unitaire")
        private BigDecimal prixUnitaire;

        @Schema(description = "Quantité")
        private Integer quantite;

        @Schema(description = "Prix total")
        private BigDecimal prixTotal;

        @Schema(description = "Unité")
        private String unite;

        @Schema(description = "ID du producteur")
        private UUID producteurId;

        @Schema(description = "Nom du producteur")
        private String nomProducteur;

        @Schema(description = "Nom de l'exploitation")
        private String nomExploitation;
    }
} 