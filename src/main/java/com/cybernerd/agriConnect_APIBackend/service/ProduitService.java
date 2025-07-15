package com.cybernerd.agriConnect_APIBackend.service;

import com.cybernerd.agriConnect_APIBackend.dtos.produit.ProduitRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.produit.ProduitResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProduitService {

    ProduitResponse creerProduit(ProduitRequest request, UUID producteurId);

    ProduitResponse modifierProduit(UUID produitId, ProduitRequest request, UUID producteurId);

    void supprimerProduit(UUID produitId, UUID producteurId);

    ProduitResponse getProduitById(UUID produitId);

    Page<ProduitResponse> getAllProduits(Pageable pageable);

    Page<ProduitResponse> rechercherProduits(
            String categorieId,
            Boolean bio,
            BigDecimal prixMin,
            BigDecimal prixMax,
            String nom,
            String origine,
            Pageable pageable
    );

    Page<ProduitResponse> getProduitsByProducteur(UUID producteurId, Pageable pageable);

    Page<ProduitResponse> getProduitsPopulaires(Pageable pageable);

    Page<ProduitResponse> getProduitsRecents(Pageable pageable);

    List<ProduitResponse> getProduitsEnRupture();

    String uploadImage(MultipartFile file, UUID produitId, UUID producteurId);

    void deleteImage(UUID produitId, String imageUrl, UUID producteurId);

    void mettreAJourStock(UUID produitId, Integer nouvelleQuantite, UUID producteurId);

    boolean verifierDisponibilite(UUID produitId, Integer quantiteDemandee);
} 