package com.cybernerd.agriConnect_APIBackend.serviceImpl;

import com.cybernerd.agriConnect_APIBackend.dtos.commande.PartenaireLogistiqueRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.commande.PartenaireLogistiqueResponse;
import com.cybernerd.agriConnect_APIBackend.enumType.StatutPartenaire;
import com.cybernerd.agriConnect_APIBackend.model.PartenaireLogistique;
import com.cybernerd.agriConnect_APIBackend.repository.PartenaireLogistiqueRepository;
import com.cybernerd.agriConnect_APIBackend.service.PartenaireLogistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PartenaireLogistiqueServiceImpl implements PartenaireLogistiqueService {
    private final PartenaireLogistiqueRepository partenaireLogistiqueRepository;

    @Override
    public PartenaireLogistiqueResponse creerPartenaire(PartenaireLogistiqueRequest request) {
        if (partenaireLogistiqueRepository.existsByNom(request.getNom())) {
            throw new RuntimeException("Nom de partenaire déjà utilisé");
        }
        if (partenaireLogistiqueRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email de partenaire déjà utilisé");
        }
        PartenaireLogistique partenaire = PartenaireLogistique.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .statut(StatutPartenaire.EN_ATTENTE)
                .dateInscription(LocalDateTime.now())
                .build();
        PartenaireLogistique saved = partenaireLogistiqueRepository.save(partenaire);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PartenaireLogistiqueResponse getPartenaireById(UUID partenaireId) {
        PartenaireLogistique partenaire = partenaireLogistiqueRepository.findById(partenaireId)
                .orElseThrow(() -> new RuntimeException("Partenaire logistique non trouvé"));
        return mapToResponse(partenaire);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartenaireLogistiqueResponse> getAllPartenaires() {
        return partenaireLogistiqueRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public PartenaireLogistiqueResponse activerPartenaire(UUID partenaireId) {
        PartenaireLogistique partenaire = partenaireLogistiqueRepository.findById(partenaireId)
                .orElseThrow(() -> new RuntimeException("Partenaire logistique non trouvé"));
        partenaire.setStatut(StatutPartenaire.ACTIF);
        PartenaireLogistique saved = partenaireLogistiqueRepository.save(partenaire);
        return mapToResponse(saved);
    }

    @Override
    public PartenaireLogistiqueResponse desactiverPartenaire(UUID partenaireId) {
        PartenaireLogistique partenaire = partenaireLogistiqueRepository.findById(partenaireId)
                .orElseThrow(() -> new RuntimeException("Partenaire logistique non trouvé"));
        partenaire.setStatut(StatutPartenaire.INACTIF);
        PartenaireLogistique saved = partenaireLogistiqueRepository.save(partenaire);
        return mapToResponse(saved);
    }

    @Override
    public void supprimerPartenaire(UUID partenaireId) {
        partenaireLogistiqueRepository.deleteById(partenaireId);
    }

    private PartenaireLogistiqueResponse mapToResponse(PartenaireLogistique partenaire) {
        PartenaireLogistiqueResponse resp = new PartenaireLogistiqueResponse();
        resp.setId(partenaire.getId());
        resp.setNom(partenaire.getNom());
        resp.setDescription(partenaire.getDescription());
        resp.setEmail(partenaire.getEmail());
        resp.setTelephone(partenaire.getTelephone());
        resp.setStatut(partenaire.getStatut());
        resp.setDateInscription(partenaire.getDateInscription());
        return resp;
    }
} 