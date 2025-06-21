package com.cybernerd.agriConnect_APIBackend.service;

import com.cybernerd.agriConnect_APIBackend.dtos.commande.CommandeRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.commande.CommandeResponse;
import com.cybernerd.agriConnect_APIBackend.enumType.StatutCommande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CommandeService {

    /**
     * Crée une nouvelle commande à partir du panier de l'acheteur
     */
    CommandeResponse creerCommande(CommandeRequest request, UUID acheteurId);

    /**
     * Récupère une commande par son ID
     */
    CommandeResponse getCommandeById(UUID commandeId);

    /**
     * Récupère toutes les commandes d'un acheteur
     */
    Page<CommandeResponse> getCommandesByAcheteur(UUID acheteurId, Pageable pageable);

    /**
     * Récupère toutes les commandes reçues par un producteur
     */
    Page<CommandeResponse> getCommandesByProducteur(UUID producteurId, Pageable pageable);

    /**
     * Met à jour le statut d'une commande
     */
    CommandeResponse mettreAJourStatut(UUID commandeId, StatutCommande nouveauStatut, UUID producteurId);

    /**
     * Recherche des commandes avec différents filtres
     */
    Page<CommandeResponse> rechercherCommandes(
            StatutCommande statut,
            String numeroCommande,
            UUID acheteurId,
            UUID producteurId,
            Pageable pageable);

    /**
     * Récupère les analytics des commandes pour un producteur
     */
    Object getAnalyticsProducteur(UUID producteurId, String periode);

    /**
     * Annule une commande
     */
    CommandeResponse annulerCommande(UUID commandeId, UUID acheteurId);

    /**
     * Récupère les commandes récentes
     */
    Page<CommandeResponse> getCommandesRecentes(Pageable pageable);

    /**
     * Vérifie si une commande peut être annulée
     */
    boolean peutEtreAnnulee(UUID commandeId);

    /**
     * Calcule les statistiques des commandes
     */
    Object getStatistiquesCommandes(UUID producteurId);
} 