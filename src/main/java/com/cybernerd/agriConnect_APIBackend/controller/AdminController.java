package com.cybernerd.agriConnect_APIBackend.controller;

import com.cybernerd.agriConnect_APIBackend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.cybernerd.agriConnect_APIBackend.enumType.StatutProfil;
import com.cybernerd.agriConnect_APIBackend.model.Utilisateur;
import com.cybernerd.agriConnect_APIBackend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "Endpoints réservés à l'administration de la plateforme")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final AdminService adminService;
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    // --- Gestion utilisateurs ---
    @Operation(
        summary = "Lister les utilisateurs", 
        description = "Retourne la liste de tous les utilisateurs de la plateforme avec pagination et filtres."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des utilisateurs récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit - Rôle ADMIN requis")
    })
    @GetMapping("/users")
    public ResponseEntity<?> listUsers() { 
        return ResponseEntity.ok(adminService.listUsers()); 
    }

    @Operation(
        summary = "Activer/désactiver un utilisateur", 
        description = "Active ou désactive un utilisateur par son ID. Un utilisateur désactivé ne peut plus se connecter."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statut de l'utilisateur mis à jour avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit - Rôle ADMIN requis"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @Parameter(description = "ID de l'utilisateur", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable String id, 
            @Parameter(description = "Actif (true) ou inactif (false)", example = "true") 
            @RequestParam boolean actif) { 
        return ResponseEntity.ok(adminService.updateUserStatus(id, actif)); 
    }

    @Operation(
        summary = "Supprimer un utilisateur", 
        description = "Supprime définitivement un utilisateur et toutes ses données associées de la plateforme."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utilisateur supprimé avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit - Rôle ADMIN requis"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "ID de l'utilisateur à supprimer", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable String id) { 
        return ResponseEntity.ok(adminService.deleteUser(id)); 
    }

    // --- Modération produits ---
    @Operation(
        summary = "Lister les produits en attente", 
        description = "Retourne la liste des produits en attente de validation par l'administrateur."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des produits en attente récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit - Rôle ADMIN requis")
    })
    @GetMapping("/products/pending")
    public ResponseEntity<?> listPendingProducts() { 
        return ResponseEntity.ok(adminService.listPendingProducts()); 
    }

    @Operation(
        summary = "Valider/refuser un produit", 
        description = "Valide ou refuse un produit par son ID. Les produits refusés ne sont pas visibles sur la plateforme."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statut du produit mis à jour avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit - Rôle ADMIN requis"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    @PatchMapping("/products/{id}/validate")
    public ResponseEntity<?> validateProduct(
            @Parameter(description = "ID du produit", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable String id, 
            @Parameter(description = "Valider (true) ou refuser (false)", example = "true") 
            @RequestParam boolean valide) { 
        return ResponseEntity.ok(adminService.validateProduct(id, valide)); 
    }

    @Operation(
        summary = "Supprimer un produit", 
        description = "Supprime définitivement un produit de la plateforme."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit supprimé avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit - Rôle ADMIN requis"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(
            @Parameter(description = "ID du produit à supprimer", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable String id) { 
        return ResponseEntity.ok(adminService.deleteProduct(id)); 
    }

    // --- Gestion commandes/litiges ---
    @Operation(
        summary = "Lister toutes les commandes", 
        description = "Retourne la liste de toutes les commandes de la plateforme avec filtres et pagination."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des commandes récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit - Rôle ADMIN requis")
    })
    @GetMapping("/orders")
    public ResponseEntity<?> listAllOrders() { 
        return ResponseEntity.ok(adminService.listAllOrders()); 
    }

    @Operation(
        summary = "Gérer un litige", 
        description = "Point d'entrée pour la gestion des litiges entre acheteurs et vendeurs."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Litige traité avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit - Rôle ADMIN requis")
    })
    @PostMapping("/disputes")
    public ResponseEntity<?> handleDispute() { 
        return ResponseEntity.ok().build(); 
    }

    // --- Statistiques/rapports ---
    @Operation(
        summary = "Obtenir les rapports/statistiques", 
        description = "Retourne les statistiques globales de la plateforme (ventes, utilisateurs, produits, etc.)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rapports récupérés avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit - Rôle ADMIN requis")
    })
    @GetMapping("/reports")
    public ResponseEntity<?> getReports() { 
        return ResponseEntity.ok(adminService.getReports()); 
    }

    // --- Gestion avis ---
    @Operation(
        summary = "Lister les avis", 
        description = "Retourne la liste de tous les avis/évaluations de la plateforme."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des avis récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit - Rôle ADMIN requis")
    })
    @GetMapping("/reviews")
    public ResponseEntity<?> listReviews() { 
        return ResponseEntity.ok(adminService.listReviews()); 
    }

    @Operation(
        summary = "Supprimer un avis", 
        description = "Supprime un avis/évaluation inapproprié de la plateforme."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Avis supprimé avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit - Rôle ADMIN requis"),
        @ApiResponse(responseCode = "404", description = "Avis non trouvé")
    })
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<?> deleteReview(
            @Parameter(description = "ID de l'avis à supprimer", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable String id) { 
        return ResponseEntity.ok(adminService.deleteReview(id)); 
    }

    // --- Gestion catégories ---
    @Operation(
        summary = "Lister les catégories", 
        description = "Retourne la liste des catégories de produits disponibles."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des catégories récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit - Rôle ADMIN requis")
    })
    @GetMapping("/categories")
    public ResponseEntity<?> listCategories() { 
        return ResponseEntity.ok(adminService.listCategories()); 
    }

    @Operation(
        summary = "Ajouter une catégorie", 
        description = "Ajoute une nouvelle catégorie de produit à la plateforme."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégorie ajoutée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit - Rôle ADMIN requis")
    })
    @PostMapping("/categories")
    public ResponseEntity<?> addCategory(
            @Parameter(description = "Nom de la catégorie", example = "Fruits exotiques") 
            @RequestParam String nom, 
            @Parameter(description = "Description de la catégorie", example = "Fruits importés et exotiques") 
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(adminService.addCategory(nom, description));
    }

    @Operation(
        summary = "Modifier une catégorie", 
        description = "Modifie une catégorie existante."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégorie modifiée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit - Rôle ADMIN requis"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    @PatchMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(
            @Parameter(description = "ID de la catégorie", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable String id, 
            @Parameter(description = "Nom de la catégorie", example = "Fruits exotiques") 
            @RequestParam String nom, 
            @Parameter(description = "Description de la catégorie", example = "Fruits importés et exotiques") 
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(adminService.updateCategory(id, nom, description));
    }

    @Operation(
        summary = "Supprimer une catégorie", 
        description = "Supprime une catégorie par son ID. Attention : les produits de cette catégorie devront être recatégorisés."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégorie supprimée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit - Rôle ADMIN requis"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée"),
        @ApiResponse(responseCode = "409", description = "Conflit - Catégorie utilisée par des produits")
    })
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(
            @Parameter(description = "ID de la catégorie à supprimer", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable String id) {
        return ResponseEntity.ok(adminService.deleteCategory(id));
    }

    @GetMapping("/demandes-producteur")
    public ResponseEntity<?> getDemandesProducteur() {
        var demandes = utilisateurRepository.findAll().stream()
            .filter(u -> u.getStatutProfil() == StatutProfil.DEMANDE_PRODUCTEUR)
            .toList();
        return ResponseEntity.ok(demandes);
    }

    @PostMapping("/valider-producteur/{userId}")
    public ResponseEntity<?> validerProducteur(@PathVariable java.util.UUID userId) {
        Utilisateur u = utilisateurRepository.findById(userId).orElseThrow();
        u.setStatutProfil(StatutProfil.PRODUCTEUR_VERIFIE);
        utilisateurRepository.save(u);
        return ResponseEntity.ok("Profil producteur validé");
    }

    @PostMapping("/refuser-producteur/{userId}")
    public ResponseEntity<?> refuserProducteur(@PathVariable java.util.UUID userId) {
        Utilisateur u = utilisateurRepository.findById(userId).orElseThrow();
        u.setStatutProfil(StatutProfil.PRODUCTEUR_REFUSE);
        utilisateurRepository.save(u);
        return ResponseEntity.ok("Demande producteur refusée");
    }
} 