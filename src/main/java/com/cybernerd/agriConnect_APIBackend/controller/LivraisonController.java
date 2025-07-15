package com.cybernerd.agriConnect_APIBackend.controller;

import com.cybernerd.agriConnect_APIBackend.dtos.commande.LivraisonRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.commande.LivraisonResponse;
import com.cybernerd.agriConnect_APIBackend.service.LivraisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/livraisons")
@RequiredArgsConstructor
public class LivraisonController {
    private final LivraisonService livraisonService;

    @PostMapping
    public ResponseEntity<LivraisonResponse> creerLivraison(@RequestBody LivraisonRequest request) {
        return ResponseEntity.ok(livraisonService.creerLivraison(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LivraisonResponse> getLivraisonById(@PathVariable UUID id) {
        return ResponseEntity.ok(livraisonService.getLivraisonById(id));
    }

    @GetMapping("/commande/{commandeId}")
    public ResponseEntity<List<LivraisonResponse>> getLivraisonsByCommande(@PathVariable UUID commandeId) {
        return ResponseEntity.ok(livraisonService.getLivraisonsByCommande(commandeId));
    }

    @GetMapping("/partenaire/{partenaireId}")
    public ResponseEntity<List<LivraisonResponse>> getLivraisonsByPartenaire(@PathVariable UUID partenaireId) {
        return ResponseEntity.ok(livraisonService.getLivraisonsByPartenaire(partenaireId));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<LivraisonResponse> mettreAJourStatut(@PathVariable UUID id, @RequestParam String statut) {
        return ResponseEntity.ok(livraisonService.mettreAJourStatut(id, statut));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerLivraison(@PathVariable UUID id) {
        livraisonService.supprimerLivraison(id);
        return ResponseEntity.noContent().build();
    }
} 