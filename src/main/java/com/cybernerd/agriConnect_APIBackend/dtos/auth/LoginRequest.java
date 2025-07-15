package com.cybernerd.agriConnect_APIBackend.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Demande de connexion utilisateur")
public class LoginRequest {

    @Schema(description = "Adresse email de l'utilisateur", example = "jean.dupont@email.com", required = true)
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @Schema(description = "Mot de passe de l'utilisateur", example = "motdepasse123", required = true)
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;
}