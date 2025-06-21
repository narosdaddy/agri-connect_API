package com.cybernerd.agriConnect_APIBackend.controller;

import com.cybernerd.agriConnect_APIBackend.dtos.commande.CommandeRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.commande.CommandeResponse;
import com.cybernerd.agriConnect_APIBackend.enumType.StatutCommande;
import com.cybernerd.agriConnect_APIBackend.service.CommandeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Commandes", description = "API pour la gestion des commandes")
@SecurityRequirement(name = "bearerAuth")
public class CommandeController {

    private final CommandeService commandeService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle commande", description = "Crée une commande à partir du panier de l'acheteur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Commande créée avec succès",
                    content = @Content(schema = @Schema(implementation = CommandeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "404", description = "Acheteur ou panier non trouvé")
    })
    @PreAuthorize("hasRole('ACHETEUR')")
    public ResponseEntity<CommandeResponse> creerCommande(
            @Parameter(description = "Données de la commande") @Valid @RequestBody CommandeRequest request,
            @Parameter(description = "ID de l'acheteur") @RequestParam UUID acheteurId) {
        
        log.info("Création d'une nouvelle commande pour l'acheteur: {}", acheteurId);
        CommandeResponse commande = commandeService.creerCommande(request, acheteurId);
        return ResponseEntity.status(HttpStatus.CREATED).body(commande);
    }

    @GetMapping("/{commandeId}")
    @Operation(summary = "Récupérer une commande par ID", description = "Récupère les détails d'une commande spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commande trouvée",
                    content = @Content(schema = @Schema(implementation = CommandeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    })
    public ResponseEntity<CommandeResponse> getCommandeById(
            @Parameter(description = "ID de la commande") @PathVariable UUID commandeId) {
        
        log.info("Récupération de la commande: {}", commandeId);
        CommandeResponse commande = commandeService.getCommandeById(commandeId);
        return ResponseEntity.ok(commande);
    }

    @GetMapping("/acheteur/{acheteurId}")
    @Operation(summary = "Récupérer les commandes d'un acheteur", description = "Récupère toutes les commandes d'un acheteur avec pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commandes récupérées avec succès"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "404", description = "Acheteur non trouvé")
    })
    @PreAuthorize("hasRole('ACHETEUR')")
    public ResponseEntity<Page<CommandeResponse>> getCommandesByAcheteur(
            @Parameter(description = "ID de l'acheteur") @PathVariable UUID acheteurId,
            @Parameter(description = "Paramètres de pagination") Pageable pageable) {
        
        log.info("Récupération des commandes de l'acheteur: {}", acheteurId);
        Page<CommandeResponse> commandes = commandeService.getCommandesByAcheteur(acheteurId, pageable);
        return ResponseEntity.ok(commandes);
    }

    @GetMapping("/producteur/{producteurId}")
    @Operation(summary = "Récupérer les commandes d'un producteur", description = "Récupère toutes les commandes reçues par un producteur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commandes récupérées avec succès"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "404", description = "Producteur non trouvé")
    })
    @PreAuthorize("hasRole('PRODUCTEUR')")
    public ResponseEntity<Page<CommandeResponse>> getCommandesByProducteur(
            @Parameter(description = "ID du producteur") @PathVariable UUID producteurId,
            @Parameter(description = "Paramètres de pagination") Pageable pageable) {
        
        log.info("Récupération des commandes du producteur: {}", producteurId);
        Page<CommandeResponse> commandes = commandeService.getCommandesByProducteur(producteurId, pageable);
        return ResponseEntity.ok(commandes);
    }

    @PutMapping("/{commandeId}/status")
    @Operation(summary = "Mettre à jour le statut d'une commande", description = "Met à jour le statut d'une commande (pour les producteurs)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut mis à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Statut invalide"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    })
    @PreAuthorize("hasRole('PRODUCTEUR')")
    public ResponseEntity<CommandeResponse> mettreAJourStatut(
            @Parameter(description = "ID de la commande") @PathVariable UUID commandeId,
            @Parameter(description = "Nouveau statut") @RequestParam StatutCommande nouveauStatut,
            @Parameter(description = "ID du producteur") @RequestParam UUID producteurId) {
        
        log.info("Mise à jour du statut de la commande: {} vers {}", commandeId, nouveauStatut);
        CommandeResponse commande = commandeService.mettreAJourStatut(commandeId, nouveauStatut, producteurId);
        return ResponseEntity.ok(commande);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des commandes", description = "Recherche des commandes avec différents filtres")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recherche effectuée avec succès"),
            @ApiResponse(responseCode = "400", description = "Paramètres de recherche invalides")
    })
    public ResponseEntity<Page<CommandeResponse>> rechercherCommandes(
            @Parameter(description = "Statut de la commande") @RequestParam(required = false) StatutCommande statut,
            @Parameter(description = "Numéro de commande") @RequestParam(required = false) String numeroCommande,
            @Parameter(description = "ID de l'acheteur") @RequestParam(required = false) UUID acheteurId,
            @Parameter(description = "ID du producteur") @RequestParam(required = false) UUID producteurId,
            @Parameter(description = "Paramètres de pagination") Pageable pageable) {
        
        log.info("Recherche de commandes avec filtres: statut={}, numeroCommande={}, acheteurId={}, producteurId={}",
                statut, numeroCommande, acheteurId, producteurId);
        
        Page<CommandeResponse> commandes = commandeService.rechercherCommandes(
                statut, numeroCommande, acheteurId, producteurId, pageable);
        return ResponseEntity.ok(commandes);
    }

    @GetMapping("/analytics/producteur/{producteurId}")
    @Operation(summary = "Analytics pour un producteur", description = "Récupère les statistiques des commandes pour un producteur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analytics récupérées avec succès"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "404", description = "Producteur non trouvé")
    })
    @PreAuthorize("hasRole('PRODUCTEUR')")
    public ResponseEntity<Object> getAnalyticsProducteur(
            @Parameter(description = "ID du producteur") @PathVariable UUID producteurId,
            @Parameter(description = "Période (7j, 30j, 90j, 1an)") @RequestParam(defaultValue = "30j") String periode) {
        
        log.info("Récupération des analytics pour le producteur: {} sur la période: {}", producteurId, periode);
        Object analytics = commandeService.getAnalyticsProducteur(producteurId, periode);
        return ResponseEntity.ok(analytics);
    }

    @PostMapping("/{commandeId}/cancel")
    @Operation(summary = "Annuler une commande", description = "Annule une commande (pour les acheteurs)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commande annulée avec succès"),
            @ApiResponse(responseCode = "400", description = "Commande non annulable"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    })
    @PreAuthorize("hasRole('ACHETEUR')")
    public ResponseEntity<CommandeResponse> annulerCommande(
            @Parameter(description = "ID de la commande") @PathVariable UUID commandeId,
            @Parameter(description = "ID de l'acheteur") @RequestParam UUID acheteurId) {
        
        log.info("Annulation de la commande: {} par l'acheteur: {}", commandeId, acheteurId);
        CommandeResponse commande = commandeService.annulerCommande(commandeId, acheteurId);
        return ResponseEntity.ok(commande);
    }

    @GetMapping("/recent")
    @Operation(summary = "Commandes récentes", description = "Récupère les commandes récentes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commandes récentes récupérées")
    })
    public ResponseEntity<Page<CommandeResponse>> getCommandesRecentes(
            @Parameter(description = "Paramètres de pagination") Pageable pageable) {
        
        log.info("Récupération des commandes récentes");
        Page<CommandeResponse> commandes = commandeService.getCommandesRecentes(pageable);
        return ResponseEntity.ok(commandes);
    }
} 