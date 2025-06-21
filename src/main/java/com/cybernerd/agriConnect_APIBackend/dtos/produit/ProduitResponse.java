package com.cybernerd.agriConnect_APIBackend.dtos.produit;

import com.cybernerd.agriConnect_APIBackend.enumType.CategorieProduit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProduitResponse {

    private UUID id;
    private String nom;
    private String description;
    private BigDecimal prix;
    private Integer quantiteDisponible;
    private CategorieProduit categorie;
    private String unite;
    private boolean bio;
    private String origine;
    private String imagePrincipale;
    private List<String> images;
    private BigDecimal noteMoyenne;
    private Integer nombreAvis;
    private boolean disponible;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    // Informations du producteur
    private UUID producteurId;
    private String nomProducteur;
    private String nomExploitation;
    private boolean producteurCertifieBio;
    private boolean producteurVerifie;
    private Double noteProducteur;
} 