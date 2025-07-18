package com.cybernerd.agriConnect_APIBackend.controller;

import com.cybernerd.agriConnect_APIBackend.dtos.auth.EvolutionProducteurRequest;
import com.cybernerd.agriConnect_APIBackend.service.ProfilService;
import com.cybernerd.agriConnect_APIBackend.service.authService.AuthService;
import com.cybernerd.agriConnect_APIBackend.model.Utilisateur;
import com.cybernerd.agriConnect_APIBackend.dtos.auth.ProfilResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/profil")
@RequiredArgsConstructor
public class ProfilController {
    private static final Logger logger = LoggerFactory.getLogger(ProfilController.class);
    private final ProfilService profilService;
    private final AuthService authService;

    @PostMapping(value = "/demande-producteur", consumes = "multipart/form-data")
    public ResponseEntity<String> demandeEvolutionProducteur(
            @ModelAttribute EvolutionProducteurRequest request,
            Principal principal) {
        profilService.demandeEvolutionProducteur(principal.getName(), request);
        return ResponseEntity.ok("Demande d'évolution vers producteur soumise avec succès");
    }

    @GetMapping("/me")
    public ResponseEntity<ProfilResponse> getMonProfil(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        logger.info("[API] Header Authorization reçu : {}", authHeader);
        logger.info("[API] Tentative de récupération du profil utilisateur courant");
        try {
            Utilisateur utilisateur = authService.getCurrentUtilisateur();
            logger.info("[API] Utilisateur authentifié : id={}, email={}, role={}", utilisateur.getId(), utilisateur.getEmail(), utilisateur.getRole());
            ProfilResponse response = ProfilResponse.builder()
                    .id(utilisateur.getId())
                    .nom(utilisateur.getNom())
                    .email(utilisateur.getEmail())
                    .telephone(utilisateur.getTelephone())
                    .role(utilisateur.getRole())
                    .build();
            logger.info("[API] ProfilResponse généré : {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[API] Erreur lors de la récupération du profil : {}", e.getMessage(), e);
            throw e;
        }
    }
} 