package com.cybernerd.agriConnect_APIBackend.service;

import com.cybernerd.agriConnect_APIBackend.dtos.commande.PartenaireLogistiqueRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.commande.PartenaireLogistiqueResponse;
import java.util.List;
import java.util.UUID;

public interface PartenaireLogistiqueService {
    PartenaireLogistiqueResponse creerPartenaire(PartenaireLogistiqueRequest request);
    PartenaireLogistiqueResponse getPartenaireById(UUID partenaireId);
    List<PartenaireLogistiqueResponse> getAllPartenaires();
    PartenaireLogistiqueResponse activerPartenaire(UUID partenaireId);
    PartenaireLogistiqueResponse desactiverPartenaire(UUID partenaireId);
    void supprimerPartenaire(UUID partenaireId);
} 