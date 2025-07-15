package com.cybernerd.agriConnect_APIBackend.model;

import com.cybernerd.agriConnect_APIBackend.enumType.StatutModerationProduit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Modèle représentant un produit agricole")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identifiant unique du produit", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Column(nullable = false)
    @Schema(description = "Nom du produit", example = "Tomates cerises bio")
    private String nom;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Description détaillée du produit", example = "Tomates cerises cultivées en agriculture biologique")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    @Schema(description = "Prix unitaire du produit", example = "4.50")
    private BigDecimal prix;

    @Column(nullable = false)
    @Schema(description = "Quantité disponible en stock", example = "100")
    private Integer quantiteDisponible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id", nullable = false)
    @Schema(description = "Catégorie du produit")
    private CategorieProduit categorie;

    @Schema(description = "Unité de mesure", example = "kg")
    private String unite; // kg, pièce, litre, etc.

    @Builder.Default
    @Schema(description = "Indique si le produit est bio", example = "true")
    private boolean bio = false;

    @Schema(description = "Origine géographique du produit", example = "France")
    private String origine;

    @Schema(description = "URL de l'image principale", example = "https://example.com/images/tomates.jpg")
    private String imagePrincipale;

    @ElementCollection
    @CollectionTable(name = "produit_images", joinColumns = @JoinColumn(name = "produit_id"))
    @Column(name = "image_url")
    @Builder.Default
    @Schema(description = "Liste des URLs des images supplémentaires")
    private List<String> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producteur_id", nullable = false)
    @Schema(description = "Producteur du produit")
    private Producteur producteur;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    @Schema(description = "Note moyenne du produit", example = "4.5")
    private BigDecimal noteMoyenne = BigDecimal.ZERO;

    @Builder.Default
    @Schema(description = "Nombre d'avis reçus", example = "25")
    private Integer nombreAvis = 0;

    @Builder.Default
    @Schema(description = "Indique si le produit est disponible", example = "true")
    private boolean disponible = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    @Schema(description = "Statut de modération du produit", example = "APPROUVE")
    private StatutModerationProduit statutModeration = StatutModerationProduit.EN_ATTENTE;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Schema(description = "Date de création du produit")
    private LocalDateTime dateCreation;

    @LastModifiedDate
    @Column(nullable = false)
    @Schema(description = "Date de dernière modification du produit")
    private LocalDateTime dateModification;

    // Méthodes utilitaires
    public void ajouterImage(String imageUrl) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(imageUrl);
    }

    public void mettreAJourNote(BigDecimal nouvelleNote) {
        if (this.noteMoyenne == null) {
            this.noteMoyenne = BigDecimal.ZERO;
        }
        if (this.nombreAvis == null) {
            this.nombreAvis = 0;
        }
        
        BigDecimal totalNotes = this.noteMoyenne.multiply(BigDecimal.valueOf(this.nombreAvis));
        this.nombreAvis++;
        this.noteMoyenne = totalNotes.add(nouvelleNote).divide(BigDecimal.valueOf(this.nombreAvis), 2, RoundingMode.HALF_UP);
    }

    public boolean estEnStock() {
        return this.disponible && this.quantiteDisponible > 0;
    }
}

