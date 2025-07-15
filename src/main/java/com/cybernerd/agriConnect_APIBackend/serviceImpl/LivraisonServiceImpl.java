package com.cybernerd.agriConnect_APIBackend.serviceImpl;

import com.cybernerd.agriConnect_APIBackend.dtos.commande.LivraisonRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.commande.LivraisonResponse;
import com.cybernerd.agriConnect_APIBackend.enumType.StatutLivraison;
import com.cybernerd.agriConnect_APIBackend.model.Commande;
import com.cybernerd.agriConnect_APIBackend.model.Livraison;
import com.cybernerd.agriConnect_APIBackend.model.PartenaireLogistique;
import com.cybernerd.agriConnect_APIBackend.repository.CommandeRepository;
import com.cybernerd.agriConnect_APIBackend.repository.LivraisonRepository;
import com.cybernerd.agriConnect_APIBackend.repository.PartenaireLogistiqueRepository;
import com.cybernerd.agriConnect_APIBackend.service.LivraisonService;
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
public class LivraisonServiceImpl implements LivraisonService {
    private final LivraisonRepository livraisonRepository;
    private final CommandeRepository commandeRepository;
    private final PartenaireLogistiqueRepository partenaireLogistiqueRepository;

    @Override
    public LivraisonResponse creerLivraison(LivraisonRequest request) {
        Commande commande = commandeRepository.findById(request.getCommandeId())
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        PartenaireLogistique partenaire = partenaireLogistiqueRepository.findById(request.getPartenaireLogistiqueId())
                .orElseThrow(() -> new RuntimeException("Partenaire logistique non trouvé"));
        Livraison livraison = Livraison.builder()
                .commande(commande)
                .partenaireLogistique(partenaire)
                .statut(StatutLivraison.EN_ATTENTE)
                .dateCreation(LocalDateTime.now())
                .dateLivraisonPrevue(request.getDateLivraisonPrevue())
                .informationsSuivi(request.getInformationsSuivi())
                .cout(request.getCout())
                .build();
        Livraison saved = livraisonRepository.save(livraison);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LivraisonResponse getLivraisonById(UUID livraisonId) {
        Livraison livraison = livraisonRepository.findById(livraisonId)
                .orElseThrow(() -> new RuntimeException("Livraison non trouvée"));
        return mapToResponse(livraison);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LivraisonResponse> getLivraisonsByCommande(UUID commandeId) {
        return livraisonRepository.findByCommandeId(commandeId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LivraisonResponse> getLivraisonsByPartenaire(UUID partenaireId) {
        return livraisonRepository.findByPartenaireLogistiqueId(partenaireId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public LivraisonResponse mettreAJourStatut(UUID livraisonId, String nouveauStatut) {
        Livraison livraison = livraisonRepository.findById(livraisonId)
                .orElseThrow(() -> new RuntimeException("Livraison non trouvée"));
        StatutLivraison statut = StatutLivraison.valueOf(nouveauStatut);
        livraison.setStatut(statut);
        if (statut == StatutLivraison.LIVREE) {
            livraison.setDateLivraisonEffective(LocalDateTime.now());
        }
        Livraison saved = livraisonRepository.save(livraison);
        return mapToResponse(saved);
    }

    @Override
    public void supprimerLivraison(UUID livraisonId) {
        livraisonRepository.deleteById(livraisonId);
    }

    private LivraisonResponse mapToResponse(Livraison livraison) {
        LivraisonResponse resp = new LivraisonResponse();
        resp.setId(livraison.getId());
        resp.setCommandeId(livraison.getCommande().getId());
        resp.setPartenaireLogistiqueId(livraison.getPartenaireLogistique().getId());
        resp.setStatut(livraison.getStatut());
        resp.setDateCreation(livraison.getDateCreation());
        resp.setDateLivraisonPrevue(livraison.getDateLivraisonPrevue());
        resp.setDateLivraisonEffective(livraison.getDateLivraisonEffective());
        resp.setInformationsSuivi(livraison.getInformationsSuivi());
        resp.setCout(livraison.getCout());
        return resp;
    }
} 