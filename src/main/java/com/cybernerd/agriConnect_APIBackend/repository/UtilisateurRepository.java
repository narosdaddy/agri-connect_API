package com.cybernerd.agriConnect_APIBackend.repository;

import com.cybernerd.agriConnect_APIBackend.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {
    Optional<Utilisateur> findByEmail(String email);
    List<Utilisateur> findAllByRole(com.cybernerd.agriConnect_APIBackend.enumType.Role role);
    List<Utilisateur> findAllByActif(boolean actif);
    List<Utilisateur> findAllByRoleAndActif(com.cybernerd.agriConnect_APIBackend.enumType.Role role, boolean actif);
    Optional<Utilisateur> findByCodeVerificationEmail(String code);
}

