package com.cybernerd.agriConnect_APIBackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "elements_commande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElementCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    private Integer quantite;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal prixUnitaire;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal prixTotal;

    private String nomProduit; // Snapshot du nom au moment de la commande

    private String descriptionProduit; // Snapshot de la description au moment de la commande

    // Méthodes utilitaires
    public void calculerPrixTotal() {
        this.prixTotal = this.prixUnitaire.multiply(BigDecimal.valueOf(this.quantite));
    }

    public void prendreSnapshotProduit() {
        if (this.produit != null) {
            this.nomProduit = this.produit.getNom();
            this.descriptionProduit = this.produit.getDescription();
        }
    }
} 