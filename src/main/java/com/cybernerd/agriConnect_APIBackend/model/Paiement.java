package com.cybernerd.agriConnect_APIBackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.LocalDate;
import com.cybernerd.agriConnect_APIBackend.enumType.MethodePaiement;

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
    @Enumerated(EnumType.STRING)
    private MethodePaiement methode;
    private String statut;

    @OneToOne
    @JoinColumn(name = "commande_id", unique = true, nullable = false)
    private Commande commande;
}
