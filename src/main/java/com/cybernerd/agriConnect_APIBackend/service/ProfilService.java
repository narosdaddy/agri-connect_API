package com.cybernerd.agriConnect_APIBackend.service;

import com.cybernerd.agriConnect_APIBackend.dtos.auth.EvolutionProducteurRequest;
import com.cybernerd.agriConnect_APIBackend.model.Utilisateur;
import java.util.UUID;

public interface ProfilService {
    void demandeEvolutionProducteur(String email, EvolutionProducteurRequest request);
    Utilisateur getProfilById(UUID profilId);
} 