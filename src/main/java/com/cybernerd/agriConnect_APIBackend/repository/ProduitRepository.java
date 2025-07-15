package com.cybernerd.agriConnect_APIBackend.repository;

import com.cybernerd.agriConnect_APIBackend.enumType.CategorieProduit;
import com.cybernerd.agriConnect_APIBackend.enumType.StatutModerationProduit;
import com.cybernerd.agriConnect_APIBackend.model.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, UUID> {

    // Recherche par catégorie
    Page<Produit> findByCategorie(CategorieProduit categorie, Pageable pageable);

    // Recherche par producteur
    Page<Produit> findByProducteurId(UUID producteurId, Pageable pageable);

    // Recherche par disponibilité
    Page<Produit> findByDisponibleTrue(Pageable pageable);

    // Recherche par produits bio
    Page<Produit> findByBioTrue(Pageable pageable);

    // Recherche par gamme de prix
    Page<Produit> findByPrixBetween(BigDecimal prixMin, BigDecimal prixMax, Pageable pageable);

    // Recherche par nom (contient)
    Page<Produit> findByNomContainingIgnoreCase(String nom, Pageable pageable);

    // Recherche par description (contient)
    Page<Produit> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    // Recherche par origine
    Page<Produit> findByOrigineContainingIgnoreCase(String origine, Pageable pageable);

    // Recherche combinée
    @Query("SELECT p FROM Produit p WHERE " +
           "(:categorie IS NULL OR p.categorie = :categorie) AND " +
           "(:bio IS NULL OR p.bio = :bio) AND " +
           "(:prixMin IS NULL OR p.prix >= :prixMin) AND " +
           "(:prixMax IS NULL OR p.prix <= :prixMax) AND " +
           "(:nom IS NULL OR LOWER(p.nom) LIKE LOWER(CONCAT('%', :nom, '%'))) AND " +
           "(:origine IS NULL OR LOWER(p.origine) LIKE LOWER(CONCAT('%', :origine, '%'))) AND " +
           "p.disponible = true")
    Page<Produit> rechercherProduits(
            @Param("categorie") CategorieProduit categorie,
            @Param("bio") Boolean bio,
            @Param("prixMin") BigDecimal prixMin,
            @Param("prixMax") BigDecimal prixMax,
            @Param("nom") String nom,
            @Param("origine") String origine,
            Pageable pageable
    );

    // Produits populaires (par note)
    @Query("SELECT p FROM Produit p WHERE p.disponible = true ORDER BY p.noteMoyenne DESC, p.nombreAvis DESC")
    Page<Produit> findProduitsPopulaires(Pageable pageable);

    // Produits récents
    @Query("SELECT p FROM Produit p WHERE p.disponible = true ORDER BY p.dateCreation DESC")
    Page<Produit> findProduitsRecents(Pageable pageable);

    // Produits par producteur avec disponibilité
    @Query("SELECT p FROM Produit p WHERE p.producteur.id = :producteurId AND p.disponible = true")
    List<Produit> findByProducteurIdAndDisponible(@Param("producteurId") UUID producteurId);

    // Compter les produits par catégorie
    @Query("SELECT p.categorie, COUNT(p) FROM Produit p WHERE p.disponible = true GROUP BY p.categorie")
    List<Object[]> countByCategorie();

    // Produits en rupture de stock
    @Query("SELECT p FROM Produit p WHERE p.quantiteDisponible = 0 AND p.disponible = true")
    List<Produit> findProduitsEnRupture();

    List<Produit> findByStatutModeration(StatutModerationProduit statutModeration);
} 