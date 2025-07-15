package com.cybernerd.agriConnect_APIBackend.serviceImpl;

import com.cybernerd.agriConnect_APIBackend.dtos.produit.ProduitRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.produit.ProduitResponse;
import com.cybernerd.agriConnect_APIBackend.model.Producteur;
import com.cybernerd.agriConnect_APIBackend.model.Produit;
import com.cybernerd.agriConnect_APIBackend.repository.ProducteurRepository;
import com.cybernerd.agriConnect_APIBackend.repository.ProduitRepository;
import com.cybernerd.agriConnect_APIBackend.repository.CategorieProduitRepository;
import com.cybernerd.agriConnect_APIBackend.model.CategorieProduit;
import com.cybernerd.agriConnect_APIBackend.service.ProduitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;
    private final ProducteurRepository producteurRepository;
    private final CategorieProduitRepository categorieProduitRepository;
    private static final String UPLOAD_DIR = "uploads/produits/";

    @Override
    public ProduitResponse creerProduit(ProduitRequest request, UUID producteurId) {
        log.info("Création d'un nouveau produit: {} par le producteur: {}", request.getNom(), producteurId);
        
        Producteur producteur = producteurRepository.findById(producteurId)
                .orElseThrow(() -> new RuntimeException("Producteur non trouvé"));
        CategorieProduit categorie = categorieProduitRepository.findById(UUID.fromString(request.getCategorieId()))
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
        Produit produit = Produit.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .prix(request.getPrix())
                .quantiteDisponible(request.getQuantiteDisponible())
                .categorie(categorie)
                .unite(request.getUnite())
                .bio(request.isBio())
                .origine(request.getOrigine())
                .imagePrincipale(request.getImagePrincipale())
                .producteur(producteur)
                .disponible(true)
                .build();

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            produit.setImages(request.getImages());
        }

        Produit savedProduit = produitRepository.save(produit);
        producteur.ajouterProduit(savedProduit);
        
        log.info("Produit créé avec succès: {}", savedProduit.getId());
        return mapToProduitResponse(savedProduit);
    }

    @Override
    public ProduitResponse modifierProduit(UUID produitId, ProduitRequest request, UUID producteurId) {
        log.info("Modification du produit: {} par le producteur: {}", produitId, producteurId);
        
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        if (!produit.getProducteur().getId().equals(producteurId)) {
            throw new RuntimeException("Accès non autorisé à ce produit");
        }

        CategorieProduit categorie = categorieProduitRepository.findById(UUID.fromString(request.getCategorieId()))
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
        produit.setNom(request.getNom());
        produit.setDescription(request.getDescription());
        produit.setPrix(request.getPrix());
        produit.setQuantiteDisponible(request.getQuantiteDisponible());
        produit.setCategorie(categorie);
        produit.setUnite(request.getUnite());
        produit.setBio(request.isBio());
        produit.setOrigine(request.getOrigine());
        produit.setImagePrincipale(request.getImagePrincipale());

        if (request.getImages() != null) {
            produit.setImages(request.getImages());
        }

        Produit updatedProduit = produitRepository.save(produit);
        log.info("Produit modifié avec succès: {}", updatedProduit.getId());
        return mapToProduitResponse(updatedProduit);
    }

    @Override
    public void supprimerProduit(UUID produitId, UUID producteurId) {
        log.info("Suppression du produit: {} par le producteur: {}", produitId, producteurId);
        
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        if (!produit.getProducteur().getId().equals(producteurId)) {
            throw new RuntimeException("Accès non autorisé à ce produit");
        }

        produit.setDisponible(false);
        produitRepository.save(produit);
        log.info("Produit supprimé avec succès: {}", produitId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProduitResponse getProduitById(UUID produitId) {
        log.info("Récupération du produit: {}", produitId);
        
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        
        return mapToProduitResponse(produit);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProduitResponse> getAllProduits(Pageable pageable) {
        log.info("Récupération de tous les produits avec pagination");
        
        Page<Produit> produits = produitRepository.findByDisponibleTrue(pageable);
        return produits.map(this::mapToProduitResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProduitResponse> rechercherProduits(
            String categorieId,
            Boolean bio,
            BigDecimal prixMin,
            BigDecimal prixMax,
            String nom,
            String origine,
            Pageable pageable) {
        log.info("Recherche de produits avec filtres: catégorieId={}, bio={}, prixMin={}, prixMax={}, nom={}, origine={}",
                categorieId, bio, prixMin, prixMax, nom, origine);
        Page<Produit> produits;
        if (categorieId != null && !categorieId.isEmpty()) {
            List<Produit> filtered = produitRepository.findAll(pageable).stream()
                .filter(p -> p.getCategorie() != null && p.getCategorie().getId().toString().equals(categorieId))
                .collect(Collectors.toList());
            produits = new org.springframework.data.domain.PageImpl<>(filtered, pageable, filtered.size());
        } else {
            produits = produitRepository.findAll(pageable);
        }
        return produits.map(this::mapToProduitResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProduitResponse> getProduitsByProducteur(UUID producteurId, Pageable pageable) {
        log.info("Récupération des produits du producteur: {}", producteurId);
        
        Page<Produit> produits = produitRepository.findByProducteurId(producteurId, pageable);
        return produits.map(this::mapToProduitResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProduitResponse> getProduitsPopulaires(Pageable pageable) {
        log.info("Récupération des produits populaires");
        
        Page<Produit> produits = produitRepository.findProduitsPopulaires(pageable);
        return produits.map(this::mapToProduitResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProduitResponse> getProduitsRecents(Pageable pageable) {
        log.info("Récupération des produits récents");
        
        Page<Produit> produits = produitRepository.findProduitsRecents(pageable);
        return produits.map(this::mapToProduitResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProduitResponse> getProduitsEnRupture() {
        log.info("Récupération des produits en rupture de stock");
        
        List<Produit> produits = produitRepository.findProduitsEnRupture();
        return produits.stream()
                .map(this::mapToProduitResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String uploadImage(MultipartFile file, UUID produitId, UUID producteurId) {
        log.info("Upload d'image pour le produit: {} par le producteur: {}", produitId, producteurId);
        
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        if (!produit.getProducteur().getId().equals(producteurId)) {
            throw new RuntimeException("Accès non autorisé à ce produit");
        }

        // Validation du type MIME
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Seuls les fichiers de type image sont acceptés (type reçu : " + contentType + ")");
        }

        try {
            // Créer le répertoire s'il n'existe pas
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Générer un nom de fichier unique
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Sauvegarder le fichier
            Files.copy(file.getInputStream(), filePath);

            // Ajouter l'image au produit
            String imageUrl = "/uploads/produits/" + fileName;
            produit.ajouterImage(imageUrl);
            produitRepository.save(produit);

            log.info("Image uploadée avec succès: {}", imageUrl);
            return imageUrl;
        } catch (IOException e) {
            log.error("Erreur lors de l'upload de l'image", e);
            throw new RuntimeException("Erreur lors de l'upload de l'image", e);
        }
    }

    @Override
    public void deleteImage(UUID produitId, String imageUrl, UUID producteurId) {
        log.info("Suppression d'image pour le produit: {} par le producteur: {}", produitId, producteurId);
        
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        if (!produit.getProducteur().getId().equals(producteurId)) {
            throw new RuntimeException("Accès non autorisé à ce produit");
        }

        produit.getImages().remove(imageUrl);
        produitRepository.save(produit);

        // Supprimer le fichier physique
        try {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.deleteIfExists(filePath);
            log.info("Image supprimée avec succès: {}", imageUrl);
        } catch (IOException e) {
            log.error("Erreur lors de la suppression du fichier", e);
        }
    }

    @Override
    public void mettreAJourStock(UUID produitId, Integer nouvelleQuantite, UUID producteurId) {
        log.info("Mise à jour du stock du produit: {} par le producteur: {}", produitId, producteurId);
        
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        if (!produit.getProducteur().getId().equals(producteurId)) {
            throw new RuntimeException("Accès non autorisé à ce produit");
        }

        if (nouvelleQuantite < 0) {
            throw new RuntimeException("La quantité ne peut pas être négative");
        }

        produit.setQuantiteDisponible(nouvelleQuantite);
        produitRepository.save(produit);
        log.info("Stock mis à jour avec succès: {} -> {}", produitId, nouvelleQuantite);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifierDisponibilite(UUID produitId, Integer quantiteDemandee) {
        log.info("Vérification de la disponibilité du produit: {} pour la quantité: {}", produitId, quantiteDemandee);
        
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        return produit.estEnStock() && produit.getQuantiteDisponible() >= quantiteDemandee;
    }

    private ProduitResponse mapToProduitResponse(Produit produit) {
        return ProduitResponse.builder()
                .id(produit.getId())
                .nom(produit.getNom())
                .description(produit.getDescription())
                .prix(produit.getPrix())
                .quantiteDisponible(produit.getQuantiteDisponible())
                .categorieId(produit.getCategorie() != null ? produit.getCategorie().getId().toString() : null)
                .categorieNom(produit.getCategorie() != null ? produit.getCategorie().getNom() : null)
                .unite(produit.getUnite())
                .bio(produit.isBio())
                .origine(produit.getOrigine())
                .imagePrincipale(produit.getImagePrincipale())
                .images(produit.getImages())
                .noteMoyenne(produit.getNoteMoyenne())
                .nombreAvis(produit.getNombreAvis())
                .disponible(produit.isDisponible())
                .dateCreation(produit.getDateCreation())
                .dateModification(produit.getDateModification())
                .producteurId(produit.getProducteur().getId())
                .nomProducteur(produit.getProducteur().getNom())
                .nomExploitation(produit.getProducteur().getNomExploitation())
                .producteurCertifieBio(produit.getProducteur().estCertifieBio())
                .producteurVerifie(produit.getProducteur().isVerifie())
                .noteProducteur(produit.getProducteur().getNoteMoyenne())
                .build();
    }
} 