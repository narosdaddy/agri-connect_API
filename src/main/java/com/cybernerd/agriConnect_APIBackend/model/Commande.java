package com.cybernerd.agriConnect_APIBackend.model;

import com.cybernerd.agriConnect_APIBackend.enumType.MethodePaiement;
import com.cybernerd.agriConnect_APIBackend.enumType.StatutCommande;
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
@Table(name = "commandes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String numeroCommande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acheteur_id", nullable = false)
    private Acheteur acheteur;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ElementCommande> elements = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    @Enumerated(EnumType.STRING)
    private MethodePaiement methodePaiement;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal sousTotal;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal fraisLivraison;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal remise;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    private String codePromo;

    private String adresseLivraison;

    private String villeLivraison;

    private String codePostalLivraison;

    private String paysLivraison;

    private String telephoneLivraison;

    private String instructionsLivraison;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime dateModification;

    private LocalDateTime dateLivraisonEstimee;

    private LocalDateTime dateLivraisonEffective;

    private Integer nombreElements;

    // Méthodes utilitaires
    public void ajouterElement(ElementCommande element) {
        this.elements.add(element);
        element.setCommande(this);
        recalculerTotaux();
    }

    public void recalculerTotaux() {
        this.sousTotal = this.elements.stream()
                .map(ElementCommande::getPrixTotal)
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

    public void changerStatut(StatutCommande nouveauStatut) {
        this.statut = nouveauStatut;
        if (nouveauStatut == StatutCommande.LIVREE) {
            this.dateLivraisonEffective = LocalDateTime.now();
        }
    }

    public boolean peutEtreAnnulee() {
        return this.statut == StatutCommande.EN_ATTENTE || this.statut == StatutCommande.EN_COURS || this.statut == StatutCommande.CONFIRMEE;
    }

    public String genererNumeroCommande() {
        return "CMD-" + System.currentTimeMillis() + "-" + this.id.toString().substring(0, 8);
    }
}

