package com.cybernerd.agriConnect_APIBackend.serviceImpl;

import com.cybernerd.agriConnect_APIBackend.dtos.auth.RegisterRequest;
import com.cybernerd.agriConnect_APIBackend.enumType.Role;
import com.cybernerd.agriConnect_APIBackend.model.Administrateur;
import com.cybernerd.agriConnect_APIBackend.model.PartenaireLogistique;
import com.cybernerd.agriConnect_APIBackend.repository.UtilisateurRepository;
import com.cybernerd.agriConnect_APIBackend.repository.PartenaireLogistiqueRepository;
import com.cybernerd.agriConnect_APIBackend.service.SuperAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SuperAdminServiceImpl implements SuperAdminService {
    private final UtilisateurRepository utilisateurRepository;
    private final PartenaireLogistiqueRepository partenaireLogistiqueRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void createUser(RegisterRequest request, Role role) {
        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }
        String motDePasseEncode = passwordEncoder.encode(request.getMotDePasse());
        if (role == Role.ADMIN || role == Role.SUPER_ADMIN) {
            Administrateur admin = new Administrateur();
            admin.setNom(request.getNom());
            admin.setEmail(request.getEmail());
            admin.setMotDePasse(motDePasseEncode);
            admin.setRole(role);
            admin.setTelephone(request.getTelephone());
            utilisateurRepository.save(admin);
        } else if (role == Role.PARTENAIRE_LOGISTIQUE) {
            PartenaireLogistique partenaire = PartenaireLogistique.builder()
                    .nom(request.getNom())
                    .email(request.getEmail())
                    .telephone(request.getTelephone())
                    .statut(com.cybernerd.agriConnect_APIBackend.enumType.StatutPartenaire.ACTIF)
                    .build();
            partenaireLogistiqueRepository.save(partenaire);
        } else {
            throw new RuntimeException("Rôle non autorisé pour la création par super admin");
        }
    }
} 