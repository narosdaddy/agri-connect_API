package com.cybernerd.agriConnect_APIBackend.dtos.produit;

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
@Schema(description = "Réponse représentant un produit agricole")
public class ProduitResponse {

    @Schema(description = "ID du produit", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;
    
    @Schema(description = "Nom du produit", example = "Tomates cerises bio")
    private String nom;
    
    @Schema(description = "Description détaillée du produit", example = "Tomates cerises cultivées en agriculture biologique")
    private String description;
    
    @Schema(description = "Prix unitaire du produit", example = "4.50")
    private BigDecimal prix;
    
    @Schema(description = "Quantité disponible en stock", example = "100")
    private Integer quantiteDisponible;
    
    @Schema(description = "ID de la catégorie", example = "LEGUMES")
    private String categorieId;
    
    @Schema(description = "Nom de la catégorie", example = "Légumes")
    private String categorieNom;
    
    @Schema(description = "Unité de mesure", example = "kg")
    private String unite;
    
    @Schema(description = "Indique si le produit est bio", example = "true")
    private boolean bio;
    
    @Schema(description = "Origine géographique du produit", example = "France")
    private String origine;
    
    @Schema(description = "URL de l'image principale", example = "https://example.com/images/tomates.jpg")
    private String imagePrincipale;
    
    @Schema(description = "Liste des URLs des images supplémentaires")
    private List<String> images;
    
    @Schema(description = "Note moyenne du produit", example = "4.5")
    private BigDecimal noteMoyenne;
    
    @Schema(description = "Nombre d'avis reçus", example = "25")
    private Integer nombreAvis;
    
    @Schema(description = "Indique si le produit est disponible", example = "true")
    private boolean disponible;
    
    @Schema(description = "Date de création du produit")
    private LocalDateTime dateCreation;
    
    @Schema(description = "Date de dernière modification du produit")
    private LocalDateTime dateModification;

    // Informations du producteur
    @Schema(description = "ID du producteur", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID producteurId;
    
    @Schema(description = "Nom du producteur", example = "Jean Dupont")
    private String nomProducteur;
    
    @Schema(description = "Nom de l'exploitation", example = "Ferme des Trois Chênes")
    private String nomExploitation;
    
    @Schema(description = "Indique si le producteur est certifié bio", example = "true")
    private boolean producteurCertifieBio;
    
    @Schema(description = "Indique si le producteur est vérifié", example = "true")
    private boolean producteurVerifie;
    
    @Schema(description = "Note moyenne du producteur", example = "4.8")
    private Double noteProducteur;
} 