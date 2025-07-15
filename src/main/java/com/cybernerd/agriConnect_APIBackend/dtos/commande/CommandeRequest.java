package com.cybernerd.agriConnect_APIBackend.dtos.commande;

import com.cybernerd.agriConnect_APIBackend.enumType.MethodePaiement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de création de commande")
public class CommandeRequest {

    @Schema(description = "Adresse de livraison", example = "123 Rue de la Paix")
    @NotBlank(message = "L'adresse de livraison est obligatoire")
    private String adresseLivraison;

    @Schema(description = "Ville de livraison", example = "Paris")
    @NotBlank(message = "La ville de livraison est obligatoire")
    private String villeLivraison;

    @Schema(description = "Code postal de livraison", example = "75001")
    @NotBlank(message = "Le code postal de livraison est obligatoire")
    private String codePostalLivraison;

    @Schema(description = "Pays de livraison", example = "France")
    @NotBlank(message = "Le pays de livraison est obligatoire")
    private String paysLivraison;

    @Schema(description = "Téléphone de livraison", example = "+33123456789")
    @NotBlank(message = "Le téléphone de livraison est obligatoire")
    private String telephoneLivraison;

    @Schema(description = "Instructions de livraison", example = "Livrer entre 14h et 18h")
    private String instructionsLivraison;

    @Schema(description = "Méthode de paiement")
    @NotNull(message = "La méthode de paiement est obligatoire")
    private MethodePaiement methodePaiement;

    @Schema(description = "Code promo appliqué")
    private String codePromo;

    @Schema(description = "Éléments de la commande")
    @NotNull(message = "Les éléments de la commande sont obligatoires")
    private List<ElementCommandeRequest> elements;

    @Schema(description = "Le client souhaite-t-il être livré ?")
    private Boolean souhaiteLivraison;

    @Schema(description = "ID du partenaire logistique choisi (optionnel)")
    private java.util.UUID partenaireLogistiqueId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Élément de commande")
    public static class ElementCommandeRequest {

        @Schema(description = "ID du produit")
        @NotNull(message = "L'ID du produit est obligatoire")
        private String produitId;

        @Schema(description = "Quantité commandée")
        @NotNull(message = "La quantité est obligatoire")
        private Integer quantite;

        @Schema(description = "Prix unitaire")
        @NotNull(message = "Le prix unitaire est obligatoire")
        private Double prixUnitaire;
    }
} 