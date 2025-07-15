package com.cybernerd.agriConnect_APIBackend.service;

import com.cybernerd.agriConnect_APIBackend.dtos.commande.PaiementRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.commande.PaiementResponse;

import java.util.List;
import java.util.UUID;

public interface PaiementService {
    PaiementResponse createPaiement(PaiementRequest request);
    PaiementResponse getPaiementById(UUID id);
    List<PaiementResponse> getAllPaiements();
} 