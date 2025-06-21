package com.cybernerd.agriConnect_APIBackend.model;

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
public class Panier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acheteur_id", nullable = false)
    private Acheteur acheteur;

    @OneToMany(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ElementPanier> elements = new ArrayList<>();

    private String codePromo;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal sousTotal = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fraisLivraison = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal remise = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @LastModifiedDate
    @Column(nullable = false)
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