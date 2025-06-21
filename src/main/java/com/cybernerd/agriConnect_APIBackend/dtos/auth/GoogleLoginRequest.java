package com.cybernerd.agriConnect_APIBackend.dtos.auth;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {

    @NotBlank
    private String idToken; // Jeton JWT reçu de Google
}
