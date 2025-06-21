package com.cybernerd.agriConnect_APIBackend.repository;

import com.cybernerd.agriConnect_APIBackend.enumType.StatutCommande;
import com.cybernerd.agriConnect_APIBackend.model.Commande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, UUID> {

    // Commandes par acheteur
    Page<Commande> findByAcheteurId(UUID acheteurId, Pageable pageable);
    
    // Commandes par acheteur ordonnées par date de création
    Page<Commande> findByAcheteurIdOrderByDateCreationDesc(UUID acheteurId, Pageable pageable);

    // Commandes par statut
    Page<Commande> findByStatut(StatutCommande statut, Pageable pageable);

    // Commandes par acheteur et statut
    Page<Commande> findByAcheteurIdAndStatut(UUID acheteurId, StatutCommande statut, Pageable pageable);

    // Commandes par producteur (via les éléments de commande)
    @Query("SELECT DISTINCT c FROM Commande c JOIN c.elements e WHERE e.produit.producteur.id = :producteurId")
    Page<Commande> findByProducteurId(@Param("producteurId") UUID producteurId, Pageable pageable);
    
    // Commandes par producteur ordonnées par date de création
    @Query("SELECT DISTINCT c FROM Commande c JOIN c.elements e WHERE e.produit.producteur.id = :producteurId ORDER BY c.dateCreation DESC")
    Page<Commande> findByProducteurIdOrderByDateCreationDesc(@Param("producteurId") UUID producteurId, Pageable pageable);

    // Commandes par producteur et statut
    @Query("SELECT DISTINCT c FROM Commande c JOIN c.elements e WHERE e.produit.producteur.id = :producteurId AND c.statut = :statut")
    Page<Commande> findByProducteurIdAndStatut(@Param("producteurId") UUID producteurId, @Param("statut") StatutCommande statut, Pageable pageable);

    // Commandes récentes
    Page<Commande> findByOrderByDateCreationDesc(Pageable pageable);

    // Commandes par période
    @Query("SELECT c FROM Commande c WHERE c.dateCreation BETWEEN :debut AND :fin")
    List<Commande> findByDateCreationBetween(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    // Recherche par numéro de commande
    Optional<Commande> findByNumeroCommande(String numeroCommande);

    // Statistiques pour un producteur
    @Query("SELECT COUNT(c), SUM(c.total) FROM Commande c JOIN c.elements e WHERE e.produit.producteur.id = :producteurId AND c.statut = 'LIVREE'")
    Object[] getStatistiquesProducteur(@Param("producteurId") UUID producteurId);

    // Commandes en attente de traitement
    @Query("SELECT c FROM Commande c WHERE c.statut IN ('EN_COURS', 'CONFIRMEE') ORDER BY c.dateCreation ASC")
    List<Commande> findCommandesEnAttente();
    
    // Recherche avancée de commandes
    @Query("SELECT c FROM Commande c WHERE " +
           "(:statut IS NULL OR c.statut = :statut) AND " +
           "(:numeroCommande IS NULL OR c.numeroCommande LIKE %:numeroCommande%) AND " +
           "(:acheteurId IS NULL OR c.acheteur.id = :acheteurId) AND " +
           "(:producteurId IS NULL OR EXISTS (SELECT 1 FROM c.elements e WHERE e.produit.producteur.id = :producteurId))")
    Page<Commande> rechercherCommandes(
            @Param("statut") StatutCommande statut,
            @Param("numeroCommande") String numeroCommande,
            @Param("acheteurId") UUID acheteurId,
            @Param("producteurId") UUID producteurId,
            Pageable pageable
    );
    
    // Compter les commandes d'un producteur dans une période
    @Query("SELECT COUNT(DISTINCT c) FROM Commande c JOIN c.elements e WHERE e.produit.producteur.id = :producteurId AND c.dateCreation BETWEEN :debut AND :fin")
    long countByProducteurAndPeriode(@Param("producteurId") UUID producteurId, @Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
    
    // Calculer le chiffre d'affaires d'un producteur dans une période
    @Query("SELECT COALESCE(SUM(c.total), 0) FROM Commande c JOIN c.elements e WHERE e.produit.producteur.id = :producteurId AND c.statut = 'LIVREE' AND c.dateCreation BETWEEN :debut AND :fin")
    BigDecimal calculerChiffreAffaires(@Param("producteurId") UUID producteurId, @Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
    
    // Obtenir les produits populaires d'un producteur
    @Query("SELECT e.produit.id, e.produit.nom, SUM(e.quantite) as totalVendu FROM Commande c JOIN c.elements e " +
           "WHERE e.produit.producteur.id = :producteurId AND c.dateCreation BETWEEN :debut AND :fin " +
           "GROUP BY e.produit.id, e.produit.nom ORDER BY totalVendu DESC")
    List<Object[]> getProduitsPopulaires(@Param("producteurId") UUID producteurId, @Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
    
    // Évolution des ventes par jour
    @Query("SELECT DATE(c.dateCreation), COUNT(c), SUM(c.total) FROM Commande c JOIN c.elements e " +
           "WHERE e.produit.producteur.id = :producteurId AND c.dateCreation BETWEEN :debut AND :fin " +
           "GROUP BY DATE(c.dateCreation) ORDER BY DATE(c.dateCreation)")
    List<Object[]> getEvolutionVentes(@Param("producteurId") UUID producteurId, @Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
    
    // Répartition des statuts de commandes
    @Query("SELECT c.statut, COUNT(c) FROM Commande c JOIN c.elements e " +
           "WHERE e.produit.producteur.id = :producteurId AND c.dateCreation BETWEEN :debut AND :fin " +
           "GROUP BY c.statut")
    List<Object[]> getStatutsCommandes(@Param("producteurId") UUID producteurId, @Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
    
    // Compter les commandes d'un producteur
    @Query("SELECT COUNT(DISTINCT c) FROM Commande c JOIN c.elements e WHERE e.produit.producteur.id = :producteurId")
    long countByProducteurId(@Param("producteurId") UUID producteurId);
    
    // Calculer le chiffre d'affaires total d'un producteur
    @Query("SELECT COALESCE(SUM(c.total), 0) FROM Commande c JOIN c.elements e WHERE e.produit.producteur.id = :producteurId AND c.statut = 'LIVREE'")
    BigDecimal calculerChiffreAffairesTotal(@Param("producteurId") UUID producteurId);
    
    // Compter les commandes en attente d'un producteur
    @Query("SELECT COUNT(DISTINCT c) FROM Commande c JOIN c.elements e WHERE e.produit.producteur.id = :producteurId AND c.statut = 'EN_ATTENTE'")
    long countByProducteurIdAndStatutEnAttente(@Param("producteurId") UUID producteurId);
    
    // Répartition des statuts pour un producteur
    @Query("SELECT c.statut, COUNT(c) FROM Commande c JOIN c.elements e " +
           "WHERE e.produit.producteur.id = :producteurId " +
           "GROUP BY c.statut")
    List<Object[]> getRepartitionStatuts(@Param("producteurId") UUID producteurId);
    
    // Top produits d'un producteur
    @Query("SELECT e.produit.id, e.produit.nom, SUM(e.quantite) as totalVendu FROM Commande c JOIN c.elements e " +
           "WHERE e.produit.producteur.id = :producteurId " +
           "GROUP BY e.produit.id, e.produit.nom ORDER BY totalVendu DESC LIMIT :limit")
    List<Object[]> getTopProduits(@Param("producteurId") UUID producteurId, @Param("limit") int limit);
    
    // Compter les commandes d'un producteur par statuts
    @Query("SELECT COUNT(DISTINCT c) FROM Commande c JOIN c.elements e " +
           "WHERE e.produit.producteur.id = :producteurId AND c.statut IN :statuts")
    long countByProducteurIdAndStatutIn(@Param("producteurId") UUID producteurId, @Param("statuts") List<StatutCommande> statuts);
} 