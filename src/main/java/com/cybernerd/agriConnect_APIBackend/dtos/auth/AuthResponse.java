package com.cybernerd.agriConnect_APIBackend.dtos.auth;

import com.cybernerd.agriConnect_APIBackend.enumType.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Réponse d'authentification contenant les informations utilisateur et tokens")
public class AuthResponse {

    @Schema(description = "ID de l'utilisateur", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;
    
    @Schema(description = "Nom de l'utilisateur", example = "Jean Dupont")
    private String nom;
    
    @Schema(description = "Email de l'utilisateur", example = "jean.dupont@email.com")
    private String email;
    
    @Schema(description = "Numéro de téléphone", example = "+33 6 12 34 56 78")
    private String telephone;
    
    @Schema(description = "Rôle de l'utilisateur", example = "ACHETEUR")
    private Role role;
    
    @Schema(description = "Token JWT d'accès", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "Token de rafraîchissement", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
    
    @Builder.Default
    @Schema(description = "Type de token", example = "Bearer")
    private String type = "Bearer";
    
    @Schema(description = "Indique si l'email est vérifié", example = "true")
    private boolean emailVerifie;
    
    @Schema(description = "Adresse de l'utilisateur", example = "123 Rue de la Paix")
    private String adresse;
    
    @Schema(description = "Ville de l'utilisateur", example = "Paris")
    private String ville;
    
    @Schema(description = "Code postal", example = "75001")
    private String codePostal;
    
    @Schema(description = "Pays de l'utilisateur", example = "France")
    private String pays;
    
    @Schema(description = "URL de l'avatar", example = "https://example.com/avatars/user.jpg")
    private String avatar;

    // Champs spécifiques aux producteurs
    @Schema(description = "Nom de l'exploitation agricole", example = "Ferme des Trois Chênes")
    private String nomExploitation;
    
    @Schema(description = "Description de l'exploitation", example = "Exploitation familiale spécialisée dans la culture bio")
    private String descriptionExploitation;
    
    @Schema(description = "Indique si l'exploitation est certifiée bio", example = "true")
    private boolean certifieBio;
    
    @Schema(description = "Indique si le producteur est vérifié", example = "true")
    private boolean verifie;
}
