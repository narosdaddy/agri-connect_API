package com.cybernerd.agriConnect_APIBackend.controller;

import com.cybernerd.agriConnect_APIBackend.dtos.panier.ElementPanierRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.panier.PanierResponse;
import com.cybernerd.agriConnect_APIBackend.service.PanierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Panier", description = "API pour la gestion du panier d'achat")
@SecurityRequirement(name = "bearerAuth")
public class PanierController {

    private final PanierService panierService;

    @Operation(summary = "Ajouter un produit au panier", description = "Ajoute un produit au panier de l'acheteur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Produit ajouté au panier",
                content = @Content(schema = @Schema(implementation = PanierResponse.class))),
        @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping
    public ResponseEntity<PanierResponse> ajouterAuPanier(
            @Parameter(description = "Données du produit à ajouter") @Valid @RequestBody ElementPanierRequest request,
            @Parameter(description = "ID de l'acheteur") @RequestParam UUID acheteurId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(panierService.ajouterAuPanier(request, acheteurId));
    }

    @Operation(summary = "Récupérer le panier d'un acheteur", description = "Récupère le contenu du panier d'un acheteur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Panier récupéré",
                content = @Content(schema = @Schema(implementation = PanierResponse.class)))
    })
    @GetMapping("/{acheteurId}")
    public ResponseEntity<PanierResponse> getPanier(
            @Parameter(description = "ID de l'acheteur") @PathVariable UUID acheteurId) {
        return ResponseEntity.ok(panierService.getPanier(acheteurId));
    }

    @Operation(summary = "Supprimer un produit du panier", description = "Retire un produit du panier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit supprimé du panier",
                content = @Content(schema = @Schema(implementation = PanierResponse.class)))
    })
    @DeleteMapping("/{acheteurId}/products/{produitId}")
    public ResponseEntity<PanierResponse> supprimerDuPanier(
            @Parameter(description = "ID de l'acheteur") @PathVariable UUID acheteurId,
            @Parameter(description = "ID du produit") @PathVariable UUID produitId) {
        return ResponseEntity.ok(panierService.supprimerDuPanier(acheteurId, produitId));
    }

    @Operation(summary = "Vider le panier", description = "Supprime tous les produits du panier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Panier vidé")
    })
    @DeleteMapping("/{acheteurId}")
    public ResponseEntity<Void> viderPanier(
            @Parameter(description = "ID de l'acheteur") @PathVariable UUID acheteurId) {
        panierService.viderPanier(acheteurId);
        return ResponseEntity.ok().build();
    }
} 