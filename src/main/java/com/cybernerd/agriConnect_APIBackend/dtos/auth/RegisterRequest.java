package com.cybernerd.agriConnect_APIBackend.dtos.auth;

import com.cybernerd.agriConnect_APIBackend.enumType.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String motDePasse;

    @Pattern(regexp = "^[0-9+\\s-]+$", message = "Format de téléphone invalide")
    private String telephone;

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    private String adresse;
    private String ville;
    private String codePostal;
    private String pays;

    // Champs spécifiques aux producteurs
    private String nomExploitation;
    private String descriptionExploitation;
    private String certificatBio;
    private boolean certifieBio;
    private String adresseExploitation;
    private String villeExploitation;
    private String codePostalExploitation;
    private String paysExploitation;
    private String telephoneExploitation;
    private String siteWeb;
}

