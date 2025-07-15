package com.cybernerd.agriConnect_APIBackend.dtos.produit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Demande de création/modification d'un produit")
public class ProduitRequest {

    @Schema(description = "Nom du produit", example = "Tomates cerises bio", required = true)
    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @Schema(description = "Description détaillée du produit", example = "Tomates cerises cultivées en agriculture biologique, récoltées à maturité", required = true)
    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 1000, message = "La description doit contenir entre 10 et 1000 caractères")
    private String description;

    @Schema(description = "Prix unitaire du produit", example = "4.50", required = true)
    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    @DecimalMax(value = "999999.99", message = "Le prix ne peut pas dépasser 999999.99")
    private BigDecimal prix;

    @Schema(description = "Quantité disponible en stock", example = "100", required = true)
    @NotNull(message = "La quantité disponible est obligatoire")
    @Min(value = 0, message = "La quantité ne peut pas être négative")
    @Max(value = 999999, message = "La quantité ne peut pas dépasser 999999")
    private Integer quantiteDisponible;

    @Schema(description = "ID de la catégorie du produit", example = "LEGUMES", required = true)
    @NotBlank(message = "La catégorie est obligatoire")
    private String categorieId;

    @Schema(description = "Unité de mesure", example = "kg", required = true)
    @NotBlank(message = "L'unité est obligatoire")
    private String unite;

    @Schema(description = "Indique si le produit est bio", example = "true")
    @Builder.Default
    private boolean bio = false;

    @Schema(description = "Origine géographique du produit", example = "France")
    private String origine;

    @Schema(description = "URL de l'image principale du produit", example = "https://example.com/images/tomates.jpg")
    private String imagePrincipale;

    @Schema(description = "Liste des URLs des images supplémentaires du produit")
    private List<String> images;
} 