package com.cybernerd.agriConnect_APIBackend.controller;

import com.cybernerd.agriConnect_APIBackend.dtos.auth.EvolutionProducteurRequest;
import com.cybernerd.agriConnect_APIBackend.service.ProfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/profil")
@RequiredArgsConstructor
public class ProfilController {
    private final ProfilService profilService;

    @PostMapping(value = "/demande-producteur", consumes = "multipart/form-data")
    public ResponseEntity<String> demandeEvolutionProducteur(
            @ModelAttribute EvolutionProducteurRequest request,
            Principal principal) {
        profilService.demandeEvolutionProducteur(principal.getName(), request);
        return ResponseEntity.ok("Demande d'évolution vers producteur soumise avec succès");
    }
} 