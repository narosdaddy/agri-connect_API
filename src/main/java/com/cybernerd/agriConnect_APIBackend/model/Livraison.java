package com.cybernerd.agriConnect_APIBackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.cybernerd.agriConnect_APIBackend.enumType.StatutLivraison;

@Entity
@Table(name = "livraisons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Livraison {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "commande_id", nullable = false, unique = true)
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partenaire_logistique_id", nullable = false)
    private PartenaireLogistique partenaireLogistique;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutLivraison statut;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateLivraisonPrevue;
    private LocalDateTime dateLivraisonEffective;
    private String informationsSuivi;
    private Double cout;
} 