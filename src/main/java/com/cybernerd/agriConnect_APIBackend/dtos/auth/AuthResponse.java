package com.cybernerd.agriConnect_APIBackend.dtos.auth;

import com.cybernerd.agriConnect_APIBackend.enumType.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private UUID id;
    private String nom;
    private String email;
    private String telephone;
    private Role role;
    private String token;
    private String refreshToken;
    @Builder.Default
    private String type = "Bearer";
    private boolean emailVerifie;
    private String adresse;
    private String ville;
    private String codePostal;
    private String pays;
    private String avatar;

    // Champs spécifiques aux producteurs
    private String nomExploitation;
    private String descriptionExploitation;
    private boolean certifieBio;
    private boolean verifie;
}
