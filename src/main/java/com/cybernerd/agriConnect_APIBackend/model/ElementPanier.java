package com.cybernerd.agriConnect_APIBackend.model;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Modèle représentant un élément dans le panier")
public class ElementPanier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identifiant unique de l'élément", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panier_id", nullable = false)
    @Schema(description = "Panier contenant cet élément")
    private Panier panier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    @Schema(description = "Produit ajouté au panier")
    private Produit produit;

    @Column(nullable = false)
    @Schema(description = "Quantité du produit", example = "2")
    private Integer quantite;

    @Column(precision = 10, scale = 2, nullable = false)
    @Schema(description = "Prix unitaire du produit", example = "4.50")
    private BigDecimal prixUnitaire;

    @Column(precision = 10, scale = 2, nullable = false)
    @Schema(description = "Prix total pour cet élément", example = "9.00")
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