package com.cybernerd.agriConnect_APIBackend.repository;

import com.cybernerd.agriConnect_APIBackend.model.Producteur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProducteurRepository extends JpaRepository<Producteur, UUID> {

    // Recherche par nom d'exploitation
    Page<Producteur> findByNomExploitationContainingIgnoreCase(String nomExploitation, Pageable pageable);

    // Recherche par certification bio
    Page<Producteur> findByCertifieBioTrue(Pageable pageable);

    // Recherche par vérification
    Page<Producteur> findByVerifieTrue(Pageable pageable);

    // Recherche par ville
    Page<Producteur> findByVilleExploitationContainingIgnoreCase(String ville, Pageable pageable);

    // Recherche combinée
    @Query("SELECT p FROM Producteur p WHERE " +
           "(:nomExploitation IS NULL OR LOWER(p.nomExploitation) LIKE LOWER(CONCAT('%', :nomExploitation, '%'))) AND " +
           "(:certifieBio IS NULL OR p.certifieBio = :certifieBio) AND " +
           "(:verifie IS NULL OR p.verifie = :verifie) AND " +
           "(:ville IS NULL OR LOWER(p.villeExploitation) LIKE LOWER(CONCAT('%', :ville, '%'))) AND " +
           "p.actif = true")
    Page<Producteur> rechercherProducteurs(
            @Param("nomExploitation") String nomExploitation,
            @Param("certifieBio") Boolean certifieBio,
            @Param("verifie") Boolean verifie,
            @Param("ville") String ville,
            Pageable pageable
    );

    // Producteurs populaires (par note)
    @Query("SELECT p FROM Producteur p WHERE p.actif = true ORDER BY p.noteMoyenne DESC, p.nombreEvaluations DESC")
    Page<Producteur> findProducteursPopulaires(Pageable pageable);

    // Producteurs récents
    @Query("SELECT p FROM Producteur p WHERE p.actif = true ORDER BY p.dateCreation DESC")
    Page<Producteur> findProducteursRecents(Pageable pageable);

    // Producteurs certifiés bio
    @Query("SELECT p FROM Producteur p WHERE p.certifieBio = true AND p.actif = true")
    List<Producteur> findProducteursCertifiesBio();

    // Producteurs vérifiés
    @Query("SELECT p FROM Producteur p WHERE p.verifie = true AND p.actif = true")
    List<Producteur> findProducteursVerifies();
} 