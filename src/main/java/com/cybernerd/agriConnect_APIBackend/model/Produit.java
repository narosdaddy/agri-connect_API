package com.cybernerd.agriConnect_APIBackend.model;

import com.cybernerd.agriConnect_APIBackend.enumType.CategorieProduit;
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
@Table(name = "produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Column(nullable = false)
    private Integer quantiteDisponible;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorieProduit categorie;

    private String unite; // kg, pièce, litre, etc.

    @Builder.Default
    private boolean bio = false;

    private String origine;

    private String imagePrincipale;

    @ElementCollection
    @CollectionTable(name = "produit_images", joinColumns = @JoinColumn(name = "produit_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producteur_id", nullable = false)
    private Producteur producteur;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal noteMoyenne = BigDecimal.ZERO;

    @Builder.Default
    private Integer nombreAvis = 0;

    @Builder.Default
    private boolean disponible = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @LastModifiedDate
    @Column(nullable = false)
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
        this.noteMoyenne = totalNotes.add(nouvelleNote).divide(BigDecimal.valueOf(this.nombreAvis), 2, BigDecimal.ROUND_HALF_UP);
    }

    public boolean estEnStock() {
        return this.disponible && this.quantiteDisponible > 0;
    }
}

