package com.cybernerd.agriConnect_APIBackend.serviceImpl;

import com.cybernerd.agriConnect_APIBackend.dtos.auth.EvolutionProducteurRequest;
import com.cybernerd.agriConnect_APIBackend.enumType.StatutProfil;
import com.cybernerd.agriConnect_APIBackend.model.DocumentJustificatif;
import com.cybernerd.agriConnect_APIBackend.model.Utilisateur;
import com.cybernerd.agriConnect_APIBackend.repository.UtilisateurRepository;
import com.cybernerd.agriConnect_APIBackend.service.ProfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfilServiceImpl implements ProfilService {
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public void demandeEvolutionProducteur(String email, EvolutionProducteurRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        // Upload des documents (mock: on stocke juste le nom du fichier)
        List<DocumentJustificatif> docs = new ArrayList<>();
        if (request.getDocumentIdentite() != null && !request.getDocumentIdentite().isEmpty()) {
            docs.add(new DocumentJustificatif("Document d'identité", request.getDocumentIdentite().getOriginalFilename(), "IDENTITE", true));
        }
        if (request.getJustificatifAdresse() != null && !request.getJustificatifAdresse().isEmpty()) {
            docs.add(new DocumentJustificatif("Justificatif d'adresse", request.getJustificatifAdresse().getOriginalFilename(), "JUSTIFICATIF_ADRESSE", true));
        }
        if (request.getCertificatBio() != null && !request.getCertificatBio().isEmpty()) {
            docs.add(new DocumentJustificatif("Certificat bio", request.getCertificatBio().getOriginalFilename(), "CERTIFICAT_BIO", false));
        }
        if (request.getAutresDocuments() != null) {
            for (MultipartFile f : request.getAutresDocuments()) {
                if (f != null && !f.isEmpty()) {
                    docs.add(new DocumentJustificatif("Autre document", f.getOriginalFilename(), "AUTRE", false));
                }
            }
        }
        utilisateur.setStatutProfil(StatutProfil.DEMANDE_PRODUCTEUR);
        utilisateur.setDocuments(docs);
        utilisateurRepository.save(utilisateur);
    }

    @Transactional(readOnly = true)
    public Utilisateur getProfilById(UUID profilId) {
        return utilisateurRepository.findById(profilId)
                .orElseThrow(() -> new RuntimeException("Profil non trouvé"));
    }
} 