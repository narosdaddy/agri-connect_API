package com.cybernerd.agriConnect_APIBackend.dtos.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationRequest {

    private String token; // Token de vérification envoyé par email
}
