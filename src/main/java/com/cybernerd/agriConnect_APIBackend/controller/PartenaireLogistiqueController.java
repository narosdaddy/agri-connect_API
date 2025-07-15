package com.cybernerd.agriConnect_APIBackend.controller;

import com.cybernerd.agriConnect_APIBackend.dtos.commande.PartenaireLogistiqueRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.commande.PartenaireLogistiqueResponse;
import com.cybernerd.agriConnect_APIBackend.service.PartenaireLogistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/partenaires-logistiques")
@RequiredArgsConstructor
public class PartenaireLogistiqueController {
    private final PartenaireLogistiqueService partenaireLogistiqueService;

    @PostMapping
    public ResponseEntity<PartenaireLogistiqueResponse> creerPartenaire(@RequestBody PartenaireLogistiqueRequest request) {
        return ResponseEntity.ok(partenaireLogistiqueService.creerPartenaire(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartenaireLogistiqueResponse> getPartenaireById(@PathVariable UUID id) {
        return ResponseEntity.ok(partenaireLogistiqueService.getPartenaireById(id));
    }

    @GetMapping
    public ResponseEntity<List<PartenaireLogistiqueResponse>> getAllPartenaires() {
        return ResponseEntity.ok(partenaireLogistiqueService.getAllPartenaires());
    }

    @PatchMapping("/{id}/activer")
    public ResponseEntity<PartenaireLogistiqueResponse> activerPartenaire(@PathVariable UUID id) {
        return ResponseEntity.ok(partenaireLogistiqueService.activerPartenaire(id));
    }

    @PatchMapping("/{id}/desactiver")
    public ResponseEntity<PartenaireLogistiqueResponse> desactiverPartenaire(@PathVariable UUID id) {
        return ResponseEntity.ok(partenaireLogistiqueService.desactiverPartenaire(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerPartenaire(@PathVariable UUID id) {
        partenaireLogistiqueService.supprimerPartenaire(id);
        return ResponseEntity.noContent().build();
    }
} 