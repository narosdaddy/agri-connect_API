package com.cybernerd.agriConnect_APIBackend.repository;

import com.cybernerd.agriConnect_APIBackend.model.ElementCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ElementCommandeRepository extends JpaRepository<ElementCommande, UUID> {

    /**
     * Trouve tous les éléments d'une commande
     */
    List<ElementCommande> findByCommandeId(UUID commandeId);

    /**
     * Trouve tous les éléments d'un produit
     */
    List<ElementCommande> findByProduitId(UUID produitId);

    /**
     * Compte le nombre d'éléments d'une commande
     */
    long countByCommandeId(UUID commandeId);

    /**
     * Trouve les éléments de commande par statut de commande
     */
    @Query("SELECT ec FROM ElementCommande ec JOIN ec.commande c WHERE c.statut = :statut")
    List<ElementCommande> findByCommandeStatut(@Param("statut") String statut);

    /**
     * Trouve les éléments de commande pour un produit spécifique dans une période donnée
     */
    @Query("SELECT ec FROM ElementCommande ec JOIN ec.commande c " +
           "WHERE ec.produit.id = :produitId " +
           "AND c.dateCreation BETWEEN :dateDebut AND :dateFin")
    List<ElementCommande> findByProduitIdAndDateBetween(
            @Param("produitId") UUID produitId,
            @Param("dateDebut") String dateDebut,
            @Param("dateFin") String dateFin
    );

    /**
     * Calcule le total des ventes d'un produit
     */
    @Query("SELECT SUM(ec.prixTotal) FROM ElementCommande ec " +
           "WHERE ec.produit.id = :produitId")
    Double getTotalVentesByProduitId(@Param("produitId") UUID produitId);

    /**
     * Trouve les produits les plus vendus
     */
    @Query("SELECT ec.produit.id, SUM(ec.quantite) as totalQuantite " +
           "FROM ElementCommande ec " +
           "GROUP BY ec.produit.id " +
           "ORDER BY totalQuantite DESC")
    List<Object[]> findProduitsLesPlusVendus();

    /**
     * Supprime tous les éléments d'une commande
     */
    void deleteByCommandeId(UUID commandeId);

    /**
     * Vérifie si un produit a des commandes
     */
    boolean existsByProduitId(UUID produitId);
} 