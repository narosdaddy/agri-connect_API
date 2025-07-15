package com.cybernerd.agriConnect_APIBackend.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "paniers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Modèle représentant le panier d'un acheteur")
public class Panier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identifiant unique du panier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acheteur_id", nullable = false)
    @Schema(description = "Acheteur propriétaire du panier")
    private Acheteur acheteur;

    @OneToMany(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @Schema(description = "Liste des éléments dans le panier")
    private List<ElementPanier> elements = new ArrayList<>();

    @Schema(description = "Code promo appliqué", example = "PROMO10")
    private String codePromo;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    @Schema(description = "Sous-total du panier", example = "25.00")
    private BigDecimal sousTotal = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    @Schema(description = "Frais de livraison", example = "5.00")
    private BigDecimal fraisLivraison = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    @Schema(description = "Montant de la remise", example = "2.00")
    private BigDecimal remise = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    @Schema(description = "Total à payer", example = "28.00")
    private BigDecimal total = BigDecimal.ZERO;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Schema(description = "Date de création du panier")
    private LocalDateTime dateCreation;

    @LastModifiedDate
    @Column(nullable = false)
    @Schema(description = "Date de dernière modification du panier")
    private LocalDateTime dateModification;

    // Méthodes utilitaires
    public void ajouterElement(ElementPanier element) {
        this.elements.add(element);
        element.setPanier(this);
        recalculerTotaux();
    }

    public void supprimerElement(ElementPanier element) {
        this.elements.remove(element);
        element.setPanier(null);
        recalculerTotaux();
    }

    public void recalculerTotaux() {
        this.sousTotal = this.elements.stream()
                .map(element -> element.getPrixUnitaire().multiply(BigDecimal.valueOf(element.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculer la remise si un code promo est appliqué
        if (this.codePromo != null && !this.codePromo.isEmpty()) {
            this.remise = calculerRemise();
        } else {
            this.remise = BigDecimal.ZERO;
        }

        this.total = this.sousTotal.add(this.fraisLivraison).subtract(this.remise);
    }

    private BigDecimal calculerRemise() {
        switch (this.codePromo.toUpperCase()) {
            case "BIENVENUE10":
                return this.sousTotal.multiply(BigDecimal.valueOf(0.10));
            case "FRESH20":
                return this.sousTotal.multiply(BigDecimal.valueOf(0.20));
            case "BIO15":
                return this.sousTotal.multiply(BigDecimal.valueOf(0.15));
            default:
                return BigDecimal.ZERO;
        }
    }

    public boolean estVide() {
        return this.elements.isEmpty();
    }

    public boolean isVide() {
        return this.elements.isEmpty();
    }

    public int getNombreElements() {
        return this.elements.size();
    }
} 