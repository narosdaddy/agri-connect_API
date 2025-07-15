package com.cybernerd.agriConnect_APIBackend.controller;

import com.cybernerd.agriConnect_APIBackend.model.Notification;
import com.cybernerd.agriConnect_APIBackend.model.Utilisateur;
import com.cybernerd.agriConnect_APIBackend.repository.UtilisateurRepository;
import com.cybernerd.agriConnect_APIBackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints pour la gestion des notifications utilisateur")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {
    private final NotificationService notificationService;
    private final UtilisateurRepository utilisateurRepository;

    @Operation(
        summary = "Lister les notifications de l'utilisateur connecté", 
        description = "Retourne la liste des notifications pour l'utilisateur authentifié, triées par date de création décroissante."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des notifications récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = Notification.class))),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit")
    })
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur user = utilisateurRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(notificationService.listerNotifications(user));
    }

    @Operation(
        summary = "Marquer une notification comme lue", 
        description = "Marque une notification spécifique comme lue par son ID. Seule l'utilisateur propriétaire de la notification peut la marquer comme lue."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification marquée comme lue avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit - Notification n'appartient pas à l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Notification non trouvée")
    })
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "ID de la notification à marquer comme lue", 
                      example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable UUID id) {
        notificationService.marquerCommeLue(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Tout marquer comme lu", 
        description = "Marque toutes les notifications non lues de l'utilisateur connecté comme lues en une seule opération."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Toutes les notifications marquées comme lues avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès interdit")
    })
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur user = utilisateurRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        notificationService.marquerToutCommeLu(user);
        return ResponseEntity.ok().build();
    }
} 