package com.cybernerd.agriConnect_APIBackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "categories_produit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class CategorieProduit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nom;

    private String description;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime dateModification;
} 