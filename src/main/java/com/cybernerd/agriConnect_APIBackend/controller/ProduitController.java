package com.cybernerd.agriConnect_APIBackend.controller;

import com.cybernerd.agriConnect_APIBackend.dtos.produit.ProduitRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.produit.ProduitResponse;
import com.cybernerd.agriConnect_APIBackend.enumType.CategorieProduit;
import com.cybernerd.agriConnect_APIBackend.service.ProduitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/produits")
@Tag(name = "Produits", description = "API pour la gestion des produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService produitService;

    @Operation(summary = "Créer un nouveau produit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Produit créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PostMapping
    @PreAuthorize("hasRole('PRODUCTEUR')")
    public ResponseEntity<ProduitResponse> creerProduit(
            @Valid @RequestBody ProduitRequest request,
            @RequestParam UUID producteurId) {
        ProduitResponse response = produitService.creerProduit(request, producteurId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Modifier un produit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit modifié avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    @PutMapping("/{produitId}")
    @PreAuthorize("hasRole('PRODUCTEUR')")
    public ResponseEntity<ProduitResponse> modifierProduit(
            @PathVariable UUID produitId,
            @Valid @RequestBody ProduitRequest request,
            @RequestParam UUID producteurId) {
        ProduitResponse response = produitService.modifierProduit(produitId, request, producteurId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Supprimer un produit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Produit supprimé avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    @DeleteMapping("/{produitId}")
    @PreAuthorize("hasRole('PRODUCTEUR')")
    public ResponseEntity<Void> supprimerProduit(
            @PathVariable UUID produitId,
            @RequestParam UUID producteurId) {
        produitService.supprimerProduit(produitId, producteurId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtenir un produit par ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit trouvé"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    @GetMapping("/{produitId}")
    public ResponseEntity<ProduitResponse> getProduitById(@PathVariable UUID produitId) {
        ProduitResponse response = produitService.getProduitById(produitId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtenir tous les produits avec pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des produits")
    })
    @GetMapping
    public ResponseEntity<Page<ProduitResponse>> getAllProduits(
            @Parameter(description = "Numéro de page (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Champ de tri") @RequestParam(defaultValue = "dateCreation") String sortBy,
            @Parameter(description = "Direction du tri") @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProduitResponse> response = produitService.getAllProduits(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Rechercher des produits avec filtres")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Résultats de recherche")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ProduitResponse>> rechercherProduits(
            @Parameter(description = "Catégorie du produit") @RequestParam(required = false) CategorieProduit categorie,
            @Parameter(description = "Produit bio") @RequestParam(required = false) Boolean bio,
            @Parameter(description = "Prix minimum") @RequestParam(required = false) BigDecimal prixMin,
            @Parameter(description = "Prix maximum") @RequestParam(required = false) BigDecimal prixMax,
            @Parameter(description = "Nom du produit") @RequestParam(required = false) String nom,
            @Parameter(description = "Origine du produit") @RequestParam(required = false) String origine,
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        String categorieId = (categorie != null) ? categorie.name() : null;
        Page<ProduitResponse> response = produitService.rechercherProduits(
                categorieId, bio, prixMin, prixMax, nom, origine, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtenir les produits d'un producteur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produits du producteur")
    })
    @GetMapping("/producteur/{producteurId}")
    public ResponseEntity<Page<ProduitResponse>> getProduitsByProducteur(
            @PathVariable UUID producteurId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProduitResponse> response = produitService.getProduitsByProducteur(producteurId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtenir les produits populaires")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produits populaires")
    })
    @GetMapping("/popular")
    public ResponseEntity<Page<ProduitResponse>> getProduitsPopulaires(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProduitResponse> response = produitService.getProduitsPopulaires(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtenir les produits récents")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produits récents")
    })
    @GetMapping("/recent")
    public ResponseEntity<Page<ProduitResponse>> getProduitsRecents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProduitResponse> response = produitService.getProduitsRecents(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtenir les produits en rupture de stock")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produits en rupture")
    })
    @GetMapping("/out-of-stock")
    @PreAuthorize("hasRole('PRODUCTEUR')")
    public ResponseEntity<List<ProduitResponse>> getProduitsEnRupture() {
        List<ProduitResponse> response = produitService.getProduitsEnRupture();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Uploader une image de produit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image uploadée avec succès"),
        @ApiResponse(responseCode = "400", description = "Fichier invalide"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PostMapping(value = "/{produitId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PRODUCTEUR')")
    public ResponseEntity<String> uploadImage(
            @PathVariable UUID produitId,
            @RequestParam("file") MultipartFile file,
            @RequestParam UUID producteurId) {
        String imageUrl = produitService.uploadImage(file, produitId, producteurId);
        return ResponseEntity.ok(imageUrl);
    }

    @Operation(summary = "Supprimer une image de produit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Image supprimée avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Image non trouvée")
    })
    @DeleteMapping("/{produitId}/images")
    @PreAuthorize("hasRole('PRODUCTEUR')")
    public ResponseEntity<Void> deleteImage(
            @PathVariable UUID produitId,
            @RequestParam String imageUrl,
            @RequestParam UUID producteurId) {
        produitService.deleteImage(produitId, imageUrl, producteurId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mettre à jour le stock d'un produit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock mis à jour"),
        @ApiResponse(responseCode = "400", description = "Quantité invalide"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    @PatchMapping("/{produitId}/stock")
    @PreAuthorize("hasRole('PRODUCTEUR')")
    public ResponseEntity<Void> mettreAJourStock(
            @PathVariable UUID produitId,
            @RequestParam Integer nouvelleQuantite,
            @RequestParam UUID producteurId) {
        produitService.mettreAJourStock(produitId, nouvelleQuantite, producteurId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Vérifier la disponibilité d'un produit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Disponibilité vérifiée")
    })
    @GetMapping("/{produitId}/availability")
    public ResponseEntity<Boolean> verifierDisponibilite(
            @PathVariable UUID produitId,
            @RequestParam Integer quantiteDemandee) {
        boolean disponible = produitService.verifierDisponibilite(produitId, quantiteDemandee);
        return ResponseEntity.ok(disponible);
    }
} 