package com.cybernerd.agriConnect_APIBackend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("ACHETEUR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Acheteur extends Utilisateur {

    @OneToOne(mappedBy = "acheteur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Panier panier;

    @OneToMany(mappedBy = "acheteur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Commande> commandes = new ArrayList<>();

    @OneToMany(mappedBy = "acheteur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Evaluation> evaluations = new ArrayList<>();

    // Méthodes utilitaires
    public void ajouterCommande(Commande commande) {
        this.commandes.add(commande);
        commande.setAcheteur(this);
    }

    public void ajouterEvaluation(Evaluation evaluation) {
        this.evaluations.add(evaluation);
        evaluation.setAcheteur(this);
    }

    public boolean aUnPanier() {
        return this.panier != null;
    }

    public void creerPanier() {
        if (this.panier == null) {
            this.panier = Panier.builder()
                    .acheteur(this)
                    .build();
        }
    }
}
