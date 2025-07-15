package com.cybernerd.agriConnect_APIBackend.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DocumentJustificatif {
    private String nom;
    private String url;
    private String type; // ex: 'IDENTITE', 'JUSTIFICATIF_ADRESSE', 'CERTIFICAT_BIO', etc.
    private boolean requis;
} 