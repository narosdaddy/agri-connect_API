package com.cybernerd.agriConnect_APIBackend.model;

import com.cybernerd.agriConnect_APIBackend.enumType.Role;
import com.cybernerd.agriConnect_APIBackend.enumType.StatutProfil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type_utilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Modèle de base pour tous les utilisateurs de la plateforme")
public abstract class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identifiant unique de l'utilisateur", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Column(nullable = false)
    @Schema(description = "Nom de l'utilisateur", example = "Jean Dupont")
    private String nom;

    @Column(nullable = false, unique = true)
    @Schema(description = "Adresse email unique de l'utilisateur", example = "jean.dupont@email.com")
    private String email;

    @Column(nullable = false)
    @Schema(description = "Mot de passe hashé de l'utilisateur")
    private String motDePasse;

    @Schema(description = "Numéro de téléphone", example = "+33 6 12 34 56 78")
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Rôle de l'utilisateur", example = "ACHETEUR")
    private Role role;

    @Builder.Default
    @Schema(description = "Indique si l'email est vérifié", example = "false")
    private boolean emailVerifie = false;

    @Schema(description = "Code de vérification d'email à 6 chiffres")
    private String codeVerificationEmail;

    @Builder.Default
    @Schema(description = "Indique si le compte provient de Google", example = "false")
    private boolean fromGoogle = false;

    @Schema(description = "Adresse de l'utilisateur", example = "123 Rue de la Paix")
    private String adresse;

    @Schema(description = "Ville de l'utilisateur", example = "Paris")
    private String ville;

    @Schema(description = "Code postal", example = "75001")
    private String codePostal;

    @Schema(description = "Pays de l'utilisateur", example = "France")
    private String pays;

    @Schema(description = "URL de l'avatar", example = "https://example.com/avatars/user.jpg")
    private String avatar;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Schema(description = "Date de création du compte")
    private LocalDateTime dateCreation;

    @LastModifiedDate
    @Column(nullable = false)
    @Schema(description = "Date de dernière modification du compte")
    private LocalDateTime dateModification;

    @Builder.Default
    @Schema(description = "Indique si le compte est actif", example = "true")
    private boolean actif = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutProfil statutProfil = StatutProfil.BASIQUE;

    @ElementCollection
    @CollectionTable(name = "utilisateur_documents", joinColumns = @JoinColumn(name = "utilisateur_id"))
    private List<DocumentJustificatif> documents;

    @Schema(description = "Date d'expiration du code de vérification d'email")
    private LocalDateTime codeVerificationExpiration;
}
