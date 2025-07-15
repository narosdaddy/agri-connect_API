package com.cybernerd.agriConnect_APIBackend.model;

import com.cybernerd.agriConnect_APIBackend.enumType.MethodePaiement;
import com.cybernerd.agriConnect_APIBackend.enumType.StatutCommande;
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
@Table(name = "commandes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Modèle représentant une commande utilisateur")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identifiant unique de la commande", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Column(unique = true, nullable = false)
    @Schema(description = "Numéro unique de la commande", example = "CMD-2024-001")
    private String numeroCommande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acheteur_id", nullable = false)
    @Schema(description = "Acheteur de la commande")
    private Acheteur acheteur;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @Schema(description = "Liste des éléments de la commande")
    private List<ElementCommande> elements = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    @Schema(description = "Statut actuel de la commande", example = "EN_ATTENTE")
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Méthode de paiement choisie", example = "CARTE_BANCAIRE")
    private MethodePaiement methodePaiement;

    @Column(precision = 10, scale = 2, nullable = false)
    @Schema(description = "Sous-total de la commande", example = "25.00")
    private BigDecimal sousTotal;

    @Column(precision = 10, scale = 2, nullable = false)
    @Schema(description = "Frais de livraison", example = "5.00")
    private BigDecimal fraisLivraison;

    @Column(precision = 10, scale = 2, nullable = false)
    @Schema(description = "Montant de la remise", example = "2.00")
    private BigDecimal remise;

    @Column(precision = 10, scale = 2, nullable = false)
    @Schema(description = "Total à payer", example = "28.00")
    private BigDecimal total;

    @Schema(description = "Code promo appliqué", example = "PROMO10")
    private String codePromo;

    @Schema(description = "Adresse de livraison", example = "123 Rue de la Paix")
    private String adresseLivraison;

    @Schema(description = "Ville de livraison", example = "Paris")
    private String villeLivraison;

    @Schema(description = "Code postal de livraison", example = "75001")
    private String codePostalLivraison;

    @Schema(description = "Pays de livraison", example = "France")
    private String paysLivraison;

    @Schema(description = "Téléphone de livraison", example = "+33123456789")
    private String telephoneLivraison;

    @Schema(description = "Instructions de livraison", example = "Livrer entre 14h et 18h")
    private String instructionsLivraison;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Schema(description = "Date de création de la commande")
    private LocalDateTime dateCreation;

    @LastModifiedDate
    @Column(nullable = false)
    @Schema(description = "Date de dernière modification de la commande")
    private LocalDateTime dateModification;

    @Schema(description = "Date de livraison estimée")
    private LocalDateTime dateLivraisonEstimee;

    @Schema(description = "Date de livraison effective")
    private LocalDateTime dateLivraisonEffective;

    @Schema(description = "Nombre total d'éléments dans la commande", example = "3")
    private Integer nombreElements;

    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Livraison livraison;

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

