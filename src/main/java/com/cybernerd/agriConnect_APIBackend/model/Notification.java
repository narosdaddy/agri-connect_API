package com.cybernerd.agriConnect_APIBackend.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Modèle représentant une notification utilisateur")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identifiant unique de la notification", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Column(nullable = false)
    @Schema(description = "Message de la notification", example = "Votre commande #CMD-2024-001 a été confirmée")
    private String message;

    @Column(nullable = false)
    @Schema(description = "Type de notification", example = "COMMANDE_CONFIRMEE")
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", nullable = false)
    @Schema(description = "Utilisateur destinataire de la notification")
    private Utilisateur destinataire;

    @Builder.Default
    @Schema(description = "Indique si la notification a été lue", example = "false")
    private boolean lu = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Schema(description = "Date de création de la notification")
    private LocalDateTime dateCreation;
} 