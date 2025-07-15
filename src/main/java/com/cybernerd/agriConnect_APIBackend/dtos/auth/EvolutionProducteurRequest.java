package com.cybernerd.agriConnect_APIBackend.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "Demande d'évolution de profil vers producteur")
public class EvolutionProducteurRequest {
    @Schema(description = "Nom de l'exploitation", required = true)
    private String nomExploitation;
    @Schema(description = "Description de l'exploitation", required = true)
    private String descriptionExploitation;
    @Schema(description = "Adresse de l'exploitation", required = true)
    private String adresseExploitation;
    @Schema(description = "Téléphone de l'exploitation", required = true)
    private String telephoneExploitation;
    @Schema(description = "Certificat bio (optionnel)")
    private MultipartFile certificatBio;
    @Schema(description = "Document d'identité (requis)")
    private MultipartFile documentIdentite;
    @Schema(description = "Justificatif d'adresse (requis)")
    private MultipartFile justificatifAdresse;
    @Schema(description = "Autres documents (optionnels)")
    private MultipartFile[] autresDocuments;
} 