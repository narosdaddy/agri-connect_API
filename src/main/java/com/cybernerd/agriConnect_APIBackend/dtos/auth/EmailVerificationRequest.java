package com.cybernerd.agriConnect_APIBackend.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Demande de vérification d'email")
public class EmailVerificationRequest {

    @Schema(description = "Code de vérification à 6 chiffres envoyé par email", example = "123456", required = true)
    private String code;
}
