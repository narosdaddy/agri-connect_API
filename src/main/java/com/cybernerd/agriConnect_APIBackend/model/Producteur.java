package com.cybernerd.agriConnect_APIBackend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("PRODUCTEUR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Producteur extends Utilisateur {

    private String nomExploitation;

    private String descriptionExploitation;

    private String certificatBio;

    @Builder.Default
    private boolean certifieBio = false;

    private String adresseExploitation;

    private String villeExploitation;

    private String codePostalExploitation;

    private String paysExploitation;

    private String telephoneExploitation;

    private String siteWeb;

    @OneToMany(mappedBy = "producteur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Produit> produits = new ArrayList<>();

    @Builder.Default
    private Double noteMoyenne = 0.0;

    @Builder.Default
    private Integer nombreEvaluations = 0;

    @Builder.Default
    private boolean verifie = false;

    // Méthodes utilitaires
    public void ajouterProduit(Produit produit) {
        this.produits.add(produit);
        produit.setProducteur(this);
    }

    public void mettreAJourNote(Double nouvelleNote) {
        if (this.noteMoyenne == null) {
            this.noteMoyenne = 0.0;
        }
        if (this.nombreEvaluations == null) {
            this.nombreEvaluations = 0;
        }
        
        double totalNotes = this.noteMoyenne * this.nombreEvaluations;
        this.nombreEvaluations++;
        this.noteMoyenne = (totalNotes + nouvelleNote) / this.nombreEvaluations;
    }

    public boolean estCertifieBio() {
        return this.certifieBio && this.certificatBio != null && !this.certificatBio.isEmpty();
    }

    public int getNombreProduitsDisponibles() {
        return (int) this.produits.stream()
                .filter(Produit::estEnStock)
                .count();
    }

    public double getChiffreAffairesTotal() {
        // Cette méthode devra être implémentée au niveau du service
        // car la relation avec les commandes se fait via les produits
        return 0.0;
    }
} 