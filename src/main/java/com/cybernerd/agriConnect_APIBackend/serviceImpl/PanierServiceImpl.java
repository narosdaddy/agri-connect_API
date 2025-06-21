package com.cybernerd.agriConnect_APIBackend.serviceImpl;

import com.cybernerd.agriConnect_APIBackend.dtos.panier.ElementPanierRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.panier.PanierResponse;
import com.cybernerd.agriConnect_APIBackend.model.Acheteur;
import com.cybernerd.agriConnect_APIBackend.model.ElementPanier;
import com.cybernerd.agriConnect_APIBackend.model.Panier;
import com.cybernerd.agriConnect_APIBackend.model.Produit;
import com.cybernerd.agriConnect_APIBackend.repository.AcheteurRepository;
import com.cybernerd.agriConnect_APIBackend.repository.ElementPanierRepository;
import com.cybernerd.agriConnect_APIBackend.repository.PanierRepository;
import com.cybernerd.agriConnect_APIBackend.repository.ProduitRepository;
import com.cybernerd.agriConnect_APIBackend.service.PanierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PanierServiceImpl implements PanierService {

    private final PanierRepository panierRepository;
    private final ElementPanierRepository elementPanierRepository;
    private final AcheteurRepository acheteurRepository;
    private final ProduitRepository produitRepository;

    @Override
    public PanierResponse getPanier(UUID acheteurId) {
        log.info("Récupération du panier pour l'acheteur: {}", acheteurId);
        
        Panier panier = getOrCreatePanier(acheteurId);
        return mapToPanierResponse(panier);
    }

    @Override
    public PanierResponse ajouterElement(UUID acheteurId, ElementPanierRequest request) {
        log.info("Ajout d'élément au panier pour l'acheteur: {}, produit: {}, quantité: {}", 
                acheteurId, request.getProduitId(), request.getQuantite());
        
        Panier panier = getOrCreatePanier(acheteurId);
        Produit produit = produitRepository.findById(request.getProduitId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        if (!produit.estEnStock()) {
            throw new RuntimeException("Produit non disponible");
        }

        if (produit.getQuantiteDisponible() < request.getQuantite()) {
            throw new RuntimeException("Quantité demandée non disponible");
        }

        // Vérifier si le produit est déjà dans le panier
        Optional<ElementPanier> existingElement = panier.getElements().stream()
                .filter(element -> element.getProduit().getId().equals(request.getProduitId()))
                .findFirst();

        if (existingElement.isPresent()) {
            // Mettre à jour la quantité
            ElementPanier element = existingElement.get();
            element.incrementerQuantite(request.getQuantite());
            elementPanierRepository.save(element);
        } else {
            // Créer un nouvel élément
            ElementPanier element = ElementPanier.builder()
                    .panier(panier)
                    .produit(produit)
                    .quantite(request.getQuantite())
                    .prixUnitaire(produit.getPrix())
                    .build();
            element.calculerPrixTotal();
            elementPanierRepository.save(element);
            panier.ajouterElement(element);
        }

        panier.recalculerTotaux();
        panierRepository.save(panier);
        
        log.info("Élément ajouté au panier avec succès");
        return mapToPanierResponse(panier);
    }

    @Override
    public PanierResponse modifierQuantite(UUID acheteurId, UUID elementId, Integer nouvelleQuantite) {
        log.info("Modification de la quantité pour l'élément: {} par l'acheteur: {}", elementId, acheteurId);
        
        ElementPanier element = elementPanierRepository.findById(elementId)
                .orElseThrow(() -> new RuntimeException("Élément du panier non trouvé"));

        if (!element.getPanier().getAcheteur().getId().equals(acheteurId)) {
            throw new RuntimeException("Accès non autorisé à cet élément");
        }

        if (nouvelleQuantite <= 0) {
            // Supprimer l'élément si la quantité est 0 ou négative
            return supprimerElement(acheteurId, elementId);
        }

        // Vérifier la disponibilité
        if (element.getProduit().getQuantiteDisponible() < nouvelleQuantite) {
            throw new RuntimeException("Quantité demandée non disponible");
        }

        element.mettreAJourQuantite(nouvelleQuantite);
        elementPanierRepository.save(element);

        Panier panier = element.getPanier();
        panier.recalculerTotaux();
        panierRepository.save(panier);
        
        log.info("Quantité modifiée avec succès: {} -> {}", elementId, nouvelleQuantite);
        return mapToPanierResponse(panier);
    }

    @Override
    public PanierResponse supprimerElement(UUID acheteurId, UUID elementId) {
        log.info("Suppression d'élément du panier: {} par l'acheteur: {}", elementId, acheteurId);
        
        ElementPanier element = elementPanierRepository.findById(elementId)
                .orElseThrow(() -> new RuntimeException("Élément du panier non trouvé"));

        if (!element.getPanier().getAcheteur().getId().equals(acheteurId)) {
            throw new RuntimeException("Accès non autorisé à cet élément");
        }

        Panier panier = element.getPanier();
        panier.supprimerElement(element);
        elementPanierRepository.delete(element);
        panierRepository.save(panier);
        
        log.info("Élément supprimé du panier avec succès: {}", elementId);
        return mapToPanierResponse(panier);
    }

    @Override
    public PanierResponse viderPanier(UUID acheteurId) {
        log.info("Vidage du panier pour l'acheteur: {}", acheteurId);
        
        Panier panier = getOrCreatePanier(acheteurId);
        
        // Supprimer tous les éléments
        elementPanierRepository.deleteAll(panier.getElements());
        panier.getElements().clear();
        panier.setCodePromo(null);
        panier.recalculerTotaux();
        panierRepository.save(panier);
        
        log.info("Panier vidé avec succès pour l'acheteur: {}", acheteurId);
        return mapToPanierResponse(panier);
    }

    @Override
    public PanierResponse appliquerCodePromo(UUID acheteurId, String codePromo) {
        log.info("Application du code promo: {} pour l'acheteur: {}", codePromo, acheteurId);
        
        Panier panier = getOrCreatePanier(acheteurId);
        
        // Vérifier si le code promo est valide
        if (!isCodePromoValide(codePromo)) {
            throw new RuntimeException("Code promo invalide");
        }

        panier.setCodePromo(codePromo.toUpperCase());
        panier.recalculerTotaux();
        panierRepository.save(panier);
        
        log.info("Code promo appliqué avec succès: {}", codePromo);
        return mapToPanierResponse(panier);
    }

    @Override
    public PanierResponse supprimerCodePromo(UUID acheteurId) {
        log.info("Suppression du code promo pour l'acheteur: {}", acheteurId);
        
        Panier panier = getOrCreatePanier(acheteurId);
        panier.setCodePromo(null);
        panier.recalculerTotaux();
        panierRepository.save(panier);
        
        log.info("Code promo supprimé avec succès");
        return mapToPanierResponse(panier);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifierDisponibilitePanier(UUID acheteurId) {
        log.info("Vérification de la disponibilité du panier pour l'acheteur: {}", acheteurId);
        
        Panier panier = getOrCreatePanier(acheteurId);
        
        for (ElementPanier element : panier.getElements()) {
            Produit produit = element.getProduit();
            if (!produit.estEnStock() || produit.getQuantiteDisponible() < element.getQuantite()) {
                log.warn("Produit non disponible: {} (demandé: {}, disponible: {})", 
                        produit.getNom(), element.getQuantite(), produit.getQuantiteDisponible());
                return false;
            }
        }
        
        return true;
    }

    @Override
    public void recalculerTotaux(UUID acheteurId) {
        log.info("Recalcul des totaux pour l'acheteur: {}", acheteurId);
        
        Panier panier = getOrCreatePanier(acheteurId);
        panier.recalculerTotaux();
        panierRepository.save(panier);
    }

    private Panier getOrCreatePanier(UUID acheteurId) {
        return panierRepository.findByAcheteurId(acheteurId)
                .orElseGet(() -> {
                    Acheteur acheteur = acheteurRepository.findById(acheteurId)
                            .orElseThrow(() -> new RuntimeException("Acheteur non trouvé"));
                    
                    Panier panier = Panier.builder()
                            .acheteur(acheteur)
                            .build();
                    
                    return panierRepository.save(panier);
                });
    }

    private boolean isCodePromoValide(String codePromo) {
        if (codePromo == null || codePromo.trim().isEmpty()) {
            return false;
        }
        
        String code = codePromo.toUpperCase();
        return code.equals("BIENVENUE10") || code.equals("FRESH20") || code.equals("BIO15");
    }

    private PanierResponse mapToPanierResponse(Panier panier) {
        List<PanierResponse.ElementPanierResponse> elements = panier.getElements().stream()
                .map(this::mapToElementPanierResponse)
                .collect(Collectors.toList());

        return PanierResponse.builder()
                .id(panier.getId())
                .elements(elements)
                .codePromo(panier.getCodePromo())
                .sousTotal(panier.getSousTotal())
                .fraisLivraison(panier.getFraisLivraison())
                .remise(panier.getRemise())
                .total(panier.getTotal())
                .dateCreation(panier.getDateCreation())
                .dateModification(panier.getDateModification())
                .nombreElements(panier.getNombreElements())
                .vide(panier.isVide())
                .build();
    }

    private PanierResponse.ElementPanierResponse mapToElementPanierResponse(ElementPanier element) {
        return PanierResponse.ElementPanierResponse.builder()
                .id(element.getId())
                .produitId(element.getProduit().getId())
                .nomProduit(element.getProduit().getNom())
                .imagePrincipale(element.getProduit().getImagePrincipale())
                .prixUnitaire(element.getPrixUnitaire())
                .quantite(element.getQuantite())
                .prixTotal(element.getPrixTotal())
                .unite(element.getProduit().getUnite())
                .disponible(element.getProduit().estEnStock())
                .build();
    }
} 