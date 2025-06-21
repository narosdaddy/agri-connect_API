package com.cybernerd.agriConnect_APIBackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "elements_panier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElementPanier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panier_id", nullable = false)
    private Panier panier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    private Integer quantite;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal prixUnitaire;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal prixTotal;

    // Méthodes utilitaires
    public void calculerPrixTotal() {
        this.prixTotal = this.prixUnitaire.multiply(BigDecimal.valueOf(this.quantite));
    }

    public void incrementerQuantite(int quantite) {
        this.quantite += quantite;
        calculerPrixTotal();
    }

    public void decrementerQuantite(int quantite) {
        if (this.quantite > quantite) {
            this.quantite -= quantite;
        } else {
            this.quantite = 0;
        }
        calculerPrixTotal();
    }

    public void mettreAJourQuantite(int nouvelleQuantite) {
        this.quantite = Math.max(0, nouvelleQuantite);
        calculerPrixTotal();
    }
} 