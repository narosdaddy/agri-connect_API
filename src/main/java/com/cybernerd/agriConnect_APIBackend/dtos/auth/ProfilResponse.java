package com.cybernerd.agriConnect_APIBackend.dtos.auth;

import com.cybernerd.agriConnect_APIBackend.enumType.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfilResponse {
    private UUID id;
    private String nom;
    private String email;
    private String telephone;
    private Role role;
    // Ajoute ici d'autres champs utiles pour le front si besoin
} 