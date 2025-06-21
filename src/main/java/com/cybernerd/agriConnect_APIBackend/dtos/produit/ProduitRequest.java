package com.cybernerd.agriConnect_APIBackend.dtos.produit;

import com.cybernerd.agriConnect_APIBackend.enumType.CategorieProduit;
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
public class ProduitRequest {

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 1000, message = "La description doit contenir entre 10 et 1000 caractères")
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    @DecimalMax(value = "999999.99", message = "Le prix ne peut pas dépasser 999999.99")
    private BigDecimal prix;

    @NotNull(message = "La quantité disponible est obligatoire")
    @Min(value = 0, message = "La quantité ne peut pas être négative")
    @Max(value = 999999, message = "La quantité ne peut pas dépasser 999999")
    private Integer quantiteDisponible;

    @NotNull(message = "La catégorie est obligatoire")
    private CategorieProduit categorie;

    @NotBlank(message = "L'unité est obligatoire")
    private String unite;

    @Builder.Default
    private boolean bio = false;

    private String origine;

    private String imagePrincipale;

    private List<String> images;
} 