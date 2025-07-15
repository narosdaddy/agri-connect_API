package com.cybernerd.agriConnect_APIBackend.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Demande d'inscription d'un utilisateur")
public class RegisterRequest {
    @Schema(description = "Nom complet de l'utilisateur", example = "Jean Dupont", required = true)
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @Schema(description = "Adresse email de l'utilisateur", example = "jean.dupont@email.com", required = true)
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @Schema(description = "Numéro de téléphone avec code pays", example = "+33 6 12 34 56 78", required = true)
    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;

    @Schema(description = "Mot de passe de l'utilisateur", example = "motdepasse123", required = true)
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String motDePasse;
}

