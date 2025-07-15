package com.cybernerd.agriConnect_APIBackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.cybernerd.agriConnect_APIBackend.enumType.StatutPartenaire;

@Entity
@Table(name = "partenaires_logistiques")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartenaireLogistique {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nom;

    private String description;

    @Column(nullable = false, unique = true)
    private String email;

    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPartenaire statut;

    @Column(nullable = false)
    private LocalDateTime dateInscription;

    @OneToMany(mappedBy = "partenaireLogistique")
    private List<Livraison> livraisons;
} 