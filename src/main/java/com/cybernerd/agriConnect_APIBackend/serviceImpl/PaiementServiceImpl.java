package com.cybernerd.agriConnect_APIBackend.serviceImpl;

import com.cybernerd.agriConnect_APIBackend.dtos.commande.PaiementRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.commande.PaiementResponse;
import com.cybernerd.agriConnect_APIBackend.model.Commande;
import com.cybernerd.agriConnect_APIBackend.model.Paiement;
import com.cybernerd.agriConnect_APIBackend.repository.CommandeRepository;
import com.cybernerd.agriConnect_APIBackend.repository.PaiementRepository;
import com.cybernerd.agriConnect_APIBackend.service.PaiementService;
import com.cybernerd.agriConnect_APIBackend.payment.PaymentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaiementServiceImpl implements PaiementService {
    private final PaiementRepository paiementRepository;
    private final CommandeRepository commandeRepository;
    @Autowired
    private List<PaymentProvider> paymentProviders;

    @Override
    @Transactional
    public PaiementResponse createPaiement(PaiementRequest request) {
        PaymentProvider provider = paymentProviders.stream()
            .filter(p -> p.supports(request.getMethode()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Méthode de paiement non supportée"));
        PaiementResponse providerResponse = provider.processPayment(request);
        Commande commande = commandeRepository.findById(request.getCommandeId())
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        Paiement paiement = new Paiement();
        paiement.setDate(providerResponse.getDate() != null ? providerResponse.getDate() : LocalDate.now());
        paiement.setMontant(request.getMontant());
        paiement.setMethode(request.getMethode());
        paiement.setStatut(providerResponse.getStatut());
        paiement.setCommande(commande);
        Paiement saved = paiementRepository.save(paiement);
        return toResponse(saved);
    }

    @Override
    public PaiementResponse getPaiementById(UUID id) {
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));
        return toResponse(paiement);
    }

    @Override
    public List<PaiementResponse> getAllPaiements() {
        return paiementRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    private PaiementResponse toResponse(Paiement paiement) {
        return PaiementResponse.builder()
                .id(paiement.getId())
                .date(paiement.getDate())
                .montant(paiement.getMontant())
                .methode(paiement.getMethode())
                .statut(paiement.getStatut())
                .commandeId(paiement.getCommande() != null ? paiement.getCommande().getId() : null)
                .build();
    }
} 