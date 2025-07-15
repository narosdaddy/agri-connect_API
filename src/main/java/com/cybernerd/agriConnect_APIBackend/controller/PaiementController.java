package com.cybernerd.agriConnect_APIBackend.controller;

import com.cybernerd.agriConnect_APIBackend.dtos.commande.PaiementRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.commande.PaiementResponse;
import com.cybernerd.agriConnect_APIBackend.service.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/paiements")
@RequiredArgsConstructor
public class PaiementController {
    private final PaiementService paiementService;

    @PostMapping
    public ResponseEntity<PaiementResponse> createPaiement(@RequestBody PaiementRequest request) {
        return ResponseEntity.ok(paiementService.createPaiement(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaiementResponse> getPaiementById(@PathVariable UUID id) {
        return ResponseEntity.ok(paiementService.getPaiementById(id));
    }

    @GetMapping
    public ResponseEntity<List<PaiementResponse>> getAllPaiements() {
        return ResponseEntity.ok(paiementService.getAllPaiements());
    }
} 