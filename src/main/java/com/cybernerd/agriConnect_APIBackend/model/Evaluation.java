package com.cybernerd.agriConnect_APIBackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acheteur_id", nullable = false)
    private Acheteur acheteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producteur_id", nullable = false)
    private Producteur producteur;

    @Column(nullable = false)
    private Integer note; // 1-5 étoiles

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Builder.Default
    private boolean verifie = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    // Méthodes utilitaires
    public boolean estNoteValide() {
        return this.note != null && this.note >= 1 && this.note <= 5;
    }

    public String getNoteEtoiles() {
        if (this.note == null) return "";
        return "★".repeat(this.note) + "☆".repeat(5 - this.note);
    }

    public void mettreAJourNote(Integer nouvelleNote) {
        this.note = nouvelleNote;
        if (this.produit != null) {
            this.produit.mettreAJourNote(java.math.BigDecimal.valueOf(nouvelleNote));
        }
        if (this.producteur != null) {
            this.producteur.mettreAJourNote(nouvelleNote.doubleValue());
        }
    }
}

