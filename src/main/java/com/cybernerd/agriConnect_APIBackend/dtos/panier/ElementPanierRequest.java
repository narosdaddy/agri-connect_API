package com.cybernerd.agriConnect_APIBackend.dtos.panier;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Requête pour ajouter un élément au panier", example = "{\n  \"produitId\": \"123e4567-e89b-12d3-a456-426614174000\",\n  \"quantite\": 2\n}")
public class ElementPanierRequest {

    @Schema(description = "ID du produit à ajouter au panier", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    @NotNull(message = "L'ID du produit est obligatoire")
    private UUID produitId;

    @Schema(description = "Quantité à ajouter", example = "2", required = true)
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins de 1")
    private Integer quantite;
} 