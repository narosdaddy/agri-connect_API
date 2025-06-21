package com.cybernerd.agriConnect_APIBackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Paiement {

    @Id
    @GeneratedValue
    private UUID id;

    private LocalDate date;
    private float montant;
    private String methode;
    private String statut;

    @OneToOne
    private Commande commande;
}
