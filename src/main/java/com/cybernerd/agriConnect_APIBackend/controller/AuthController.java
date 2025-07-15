package com.cybernerd.agriConnect_APIBackend.controller;

import com.cybernerd.agriConnect_APIBackend.dtos.auth.AuthResponse;
import com.cybernerd.agriConnect_APIBackend.dtos.auth.LoginRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.auth.RegisterRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.auth.EmailVerificationRequest;
import com.cybernerd.agriConnect_APIBackend.service.authService.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("auth")
@Tag(name = "Authentification", description = "API pour la gestion de l'authentification")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Operation(summary = "Inscription d'un utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Utilisateur enregistré avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Tentative d'inscription pour l'utilisateur: {}", request.getEmail());
        try {
            AuthResponse response = authService.registerUser(request);
            logger.info("Inscription réussie pour l'utilisateur: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Erreur d'inscription pour l'utilisateur {}: {}", request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Connexion d'un utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Connexion réussie"),
        @ApiResponse(responseCode = "401", description = "Identifiants invalides"),
        @ApiResponse(responseCode = "403", description = "Email non vérifié")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Tentative de connexion pour l'utilisateur: {}", request.getEmail());
        try {
            AuthResponse response = authService.loginUser(request);
            logger.info("Connexion réussie pour l'utilisateur: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur de connexion pour l'utilisateur {}: {}", request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Vérification de l'email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email vérifié avec succès"),
        @ApiResponse(responseCode = "400", description = "Code invalide ou expiré")
    })
    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestBody EmailVerificationRequest request) {
        try {
            authService.verifyEmail(request.getCode());
            return ResponseEntity.ok("Email vérifié avec succès");
        } catch (Exception e) {
            logger.error("Erreur de vérification d'email: ", e);
            throw e;
        }
    }

    @Operation(summary = "Renvoyer l'email de vérification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email de vérification envoyé"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@RequestParam String email) {
        try {
            authService.resendVerificationEmail(email);
            return ResponseEntity.ok("Email de vérification envoyé");
        } catch (Exception e) {
            logger.error("Erreur d'envoi d'email de vérification: ", e);
            throw e;
        }
    }

    @Operation(summary = "Rafraîchir le token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token rafraîchi"),
        @ApiResponse(responseCode = "401", description = "Token invalide")
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam String token) {
        try {
            AuthResponse response = authService.refreshToken(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur de rafraîchissement de token: ", e);
            throw e;
        }
    }

    @Operation(summary = "Envoyer un email de réinitialisation de mot de passe")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email envoyé"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<String> sendPasswordResetEmail(@RequestParam String email) {
        try {
            authService.sendPasswordResetEmail(email);
            return ResponseEntity.ok("Email de réinitialisation envoyé");
        } catch (Exception e) {
            logger.error("Erreur d'envoi d'email de réinitialisation: ", e);
            throw e;
        }
    }

    @Operation(summary = "Vérifier si l'email est vérifié")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statut de vérification retourné")
    })
    @GetMapping("/email-verified")
    public ResponseEntity<Boolean> isEmailVerified(@RequestParam String email) {
        boolean verified = authService.isEmailVerified(email);
        return ResponseEntity.ok(verified);
    }

}