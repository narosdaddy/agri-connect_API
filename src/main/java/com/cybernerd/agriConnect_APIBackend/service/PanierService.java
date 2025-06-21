package com.cybernerd.agriConnect_APIBackend.service;

import com.cybernerd.agriConnect_APIBackend.dtos.panier.ElementPanierRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.panier.PanierResponse;

import java.util.UUID;

public interface PanierService {

    PanierResponse getPanier(UUID acheteurId);

    PanierResponse ajouterElement(UUID acheteurId, ElementPanierRequest request);

    PanierResponse modifierQuantite(UUID acheteurId, UUID elementId, Integer nouvelleQuantite);

    PanierResponse supprimerElement(UUID acheteurId, UUID elementId);

    PanierResponse viderPanier(UUID acheteurId);

    PanierResponse appliquerCodePromo(UUID acheteurId, String codePromo);

    PanierResponse supprimerCodePromo(UUID acheteurId);

    boolean verifierDisponibilitePanier(UUID acheteurId);

    void recalculerTotaux(UUID acheteurId);
} 