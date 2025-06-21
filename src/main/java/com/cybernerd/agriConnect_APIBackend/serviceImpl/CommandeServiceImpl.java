package com.cybernerd.agriConnect_APIBackend.serviceImpl;

import com.cybernerd.agriConnect_APIBackend.dtos.commande.CommandeRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.commande.CommandeResponse;
import com.cybernerd.agriConnect_APIBackend.dtos.panier.PanierResponse;
import com.cybernerd.agriConnect_APIBackend.enumType.StatutCommande;
import com.cybernerd.agriConnect_APIBackend.model.Acheteur;
import com.cybernerd.agriConnect_APIBackend.model.Commande;
import com.cybernerd.agriConnect_APIBackend.model.ElementCommande;
import com.cybernerd.agriConnect_APIBackend.model.Produit;
import com.cybernerd.agriConnect_APIBackend.repository.AcheteurRepository;
import com.cybernerd.agriConnect_APIBackend.repository.CommandeRepository;
import com.cybernerd.agriConnect_APIBackend.repository.ElementCommandeRepository;
import com.cybernerd.agriConnect_APIBackend.repository.ProduitRepository;
import com.cybernerd.agriConnect_APIBackend.service.CommandeService;
import com.cybernerd.agriConnect_APIBackend.service.PanierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommandeServiceImpl implements CommandeService {

    private final CommandeRepository commandeRepository;
    private final ElementCommandeRepository elementCommandeRepository;
    private final AcheteurRepository acheteurRepository;
    private final ProduitRepository produitRepository;
    private final PanierService panierService;

    private static final BigDecimal FRAIS_LIVRAISON_STANDARD = new BigDecimal("5.00");
    private static final BigDecimal FRAIS_LIVRAISON_GRATUIT = new BigDecimal("0.00");
    private static final BigDecimal SEUIL_LIVRAISON_GRATUITE = new BigDecimal("50.00");

    @Override
    public CommandeResponse creerCommande(CommandeRequest request, UUID acheteurId) {
        log.info("Création d'une nouvelle commande pour l'acheteur: {}", acheteurId);

        // Vérifier que l'acheteur existe
        Acheteur acheteur = acheteurRepository.findById(acheteurId)
                .orElseThrow(() -> new RuntimeException("Acheteur non trouvé"));

        // Récupérer le panier de l'acheteur
        PanierResponse panier = panierService.getPanier(acheteurId);
        
        if (panier.isVide()) {
            throw new RuntimeException("Le panier est vide");
        }

        // Vérifier la disponibilité des produits
        if (!panierService.verifierDisponibilitePanier(acheteurId)) {
            throw new RuntimeException("Certains produits ne sont plus disponibles");
        }

        // Créer la commande
        Commande commande = Commande.builder()
                .numeroCommande(genererNumeroCommande())
                .acheteur(acheteur)
                .statut(StatutCommande.EN_ATTENTE)
                .methodePaiement(request.getMethodePaiement())
                .adresseLivraison(request.getAdresseLivraison())
                .villeLivraison(request.getVilleLivraison())
                .codePostalLivraison(request.getCodePostalLivraison())
                .paysLivraison(request.getPaysLivraison())
                .telephoneLivraison(request.getTelephoneLivraison())
                .instructionsLivraison(request.getInstructionsLivraison())
                .codePromo(request.getCodePromo())
                .sousTotal(panier.getSousTotal())
                .fraisLivraison(calculerFraisLivraison(panier.getSousTotal()))
                .remise(panier.getRemise())
                .total(calculerTotal(panier))
                .dateLivraisonEstimee(LocalDateTime.now().plusDays(3))
                .build();

        // Créer les éléments de commande
        List<ElementCommande> elements = panier.getElements().stream()
                .map(element -> creerElementCommande(element, commande))
                .collect(Collectors.toList());

        commande.setElements(elements);
        commande.setNombreElements(elements.size());

        // Sauvegarder la commande
        Commande savedCommande = commandeRepository.save(commande);
        elementCommandeRepository.saveAll(elements);

        // Mettre à jour les stocks
        mettreAJourStocks(elements);

        // Vider le panier
        panierService.viderPanier(acheteurId);

        log.info("Commande créée avec succès: {}", savedCommande.getNumeroCommande());
        return mapToCommandeResponse(savedCommande);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandeResponse getCommandeById(UUID commandeId) {
        log.info("Récupération de la commande: {}", commandeId);
        
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        
        return mapToCommandeResponse(commande);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommandeResponse> getCommandesByAcheteur(UUID acheteurId, Pageable pageable) {
        log.info("Récupération des commandes de l'acheteur: {}", acheteurId);
        
        Page<Commande> commandes = commandeRepository.findByAcheteurIdOrderByDateCreationDesc(acheteurId, pageable);
        return commandes.map(this::mapToCommandeResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommandeResponse> getCommandesByProducteur(UUID producteurId, Pageable pageable) {
        log.info("Récupération des commandes du producteur: {}", producteurId);
        
        Page<Commande> commandes = commandeRepository.findByProducteurIdOrderByDateCreationDesc(producteurId, pageable);
        return commandes.map(this::mapToCommandeResponse);
    }

    @Override
    public CommandeResponse mettreAJourStatut(UUID commandeId, StatutCommande nouveauStatut, UUID producteurId) {
        log.info("Mise à jour du statut de la commande: {} vers {}", commandeId, nouveauStatut);
        
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        // Vérifier que le producteur a le droit de modifier cette commande
        boolean peutModifier = commande.getElements().stream()
                .anyMatch(element -> element.getProduit().getProducteur().getId().equals(producteurId));
        
        if (!peutModifier) {
            throw new RuntimeException("Accès non autorisé à cette commande");
        }

        // Mettre à jour le statut
        commande.setStatut(nouveauStatut);
        commande.setDateModification(LocalDateTime.now());

        // Si la commande est livrée, mettre à jour la date de livraison
        if (nouveauStatut == StatutCommande.LIVREE) {
            commande.setDateLivraisonEffective(LocalDateTime.now());
        }

        Commande updatedCommande = commandeRepository.save(commande);
        log.info("Statut de la commande mis à jour: {} -> {}", commandeId, nouveauStatut);
        
        return mapToCommandeResponse(updatedCommande);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommandeResponse> rechercherCommandes(
            StatutCommande statut,
            String numeroCommande,
            UUID acheteurId,
            UUID producteurId,
            Pageable pageable) {
        
        log.info("Recherche de commandes avec filtres: statut={}, numeroCommande={}, acheteurId={}, producteurId={}",
                statut, numeroCommande, acheteurId, producteurId);
        
        Page<Commande> commandes = commandeRepository.rechercherCommandes(
                statut, numeroCommande, acheteurId, producteurId, pageable);
        
        return commandes.map(this::mapToCommandeResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getAnalyticsProducteur(UUID producteurId, String periode) {
        log.info("Récupération des analytics pour le producteur: {} sur la période: {}", producteurId, periode);
        
        LocalDateTime dateDebut = calculerDateDebut(periode);
        LocalDateTime dateFin = LocalDateTime.now();

        Map<String, Object> analytics = new HashMap<>();
        
        // Statistiques générales
        analytics.put("periode", periode);
        analytics.put("dateDebut", dateDebut);
        analytics.put("dateFin", dateFin);
        
        // Nombre de commandes
        long nombreCommandes = commandeRepository.countByProducteurAndPeriode(producteurId, dateDebut, dateFin);
        analytics.put("nombreCommandes", nombreCommandes);
        
        // Chiffre d'affaires
        BigDecimal chiffreAffaires = commandeRepository.calculerChiffreAffaires(producteurId, dateDebut, dateFin);
        analytics.put("chiffreAffaires", chiffreAffaires);
        
        // Produits les plus vendus
        List<Object[]> produitsPopulaires = commandeRepository.getProduitsPopulaires(producteurId, dateDebut, dateFin);
        analytics.put("produitsPopulaires", produitsPopulaires);
        
        // Évolution des ventes par jour
        List<Object[]> evolutionVentes = commandeRepository.getEvolutionVentes(producteurId, dateDebut, dateFin);
        analytics.put("evolutionVentes", evolutionVentes);
        
        // Statuts des commandes
        List<Object[]> statutsCommandes = commandeRepository.getStatutsCommandes(producteurId, dateDebut, dateFin);
        analytics.put("statutsCommandes", statutsCommandes);
        
        return analytics;
    }

    @Override
    public CommandeResponse annulerCommande(UUID commandeId, UUID acheteurId) {
        log.info("Annulation de la commande: {} par l'acheteur: {}", commandeId, acheteurId);
        
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        if (!commande.getAcheteur().getId().equals(acheteurId)) {
            throw new RuntimeException("Accès non autorisé à cette commande");
        }

        if (!peutEtreAnnulee(commandeId)) {
            throw new RuntimeException("Cette commande ne peut plus être annulée");
        }

        // Mettre à jour le statut
        commande.setStatut(StatutCommande.ANNULEE);
        commande.setDateModification(LocalDateTime.now());

        // Restaurer les stocks
        restaurerStocks(commande.getElements());

        Commande updatedCommande = commandeRepository.save(commande);
        log.info("Commande annulée avec succès: {}", commandeId);
        
        return mapToCommandeResponse(updatedCommande);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommandeResponse> getCommandesRecentes(Pageable pageable) {
        log.info("Récupération des commandes récentes");
        
        Page<Commande> commandes = commandeRepository.findByOrderByDateCreationDesc(pageable);
        return commandes.map(this::mapToCommandeResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean peutEtreAnnulee(UUID commandeId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        
        return commande.peutEtreAnnulee();
    }

    @Override
    @Transactional(readOnly = true)
    public Object getStatistiquesCommandes(UUID producteurId) {
        log.info("Récupération des statistiques pour le producteur: {}", producteurId);
        
        Map<String, Object> statistiques = new HashMap<>();
        
        // Statistiques générales
        long totalCommandes = commandeRepository.countByProducteurId(producteurId);
        BigDecimal chiffreAffairesTotal = commandeRepository.calculerChiffreAffairesTotal(producteurId);
        long commandesEnAttente = commandeRepository.countByProducteurIdAndStatutEnAttente(producteurId);
        
        statistiques.put("totalCommandes", totalCommandes);
        statistiques.put("chiffreAffairesTotal", chiffreAffairesTotal);
        statistiques.put("commandesEnAttente", commandesEnAttente);
        
        // Répartition des statuts
        List<Object[]> repartitionStatuts = commandeRepository.getRepartitionStatuts(producteurId);
        statistiques.put("repartitionStatuts", repartitionStatuts);
        
        // Top produits
        List<Object[]> topProduits = commandeRepository.getTopProduits(producteurId, 5);
        statistiques.put("topProduits", topProduits);
        
        return statistiques;
    }

    // Méthodes utilitaires privées
    private String genererNumeroCommande() {
        return "CMD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private BigDecimal calculerFraisLivraison(BigDecimal sousTotal) {
        return sousTotal.compareTo(SEUIL_LIVRAISON_GRATUITE) >= 0 ? 
                FRAIS_LIVRAISON_GRATUIT : FRAIS_LIVRAISON_STANDARD;
    }

    private BigDecimal calculerTotal(PanierResponse panier) {
        return panier.getSousTotal().add(panier.getFraisLivraison()).subtract(panier.getRemise());
    }

    private ElementCommande creerElementCommande(PanierResponse.ElementPanierResponse elementPanier, Commande commande) {
        Produit produit = produitRepository.findById(elementPanier.getProduitId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        return ElementCommande.builder()
                .commande(commande)
                .produit(produit)
                .quantite(elementPanier.getQuantite())
                .prixUnitaire(elementPanier.getPrixUnitaire())
                .prixTotal(elementPanier.getPrixTotal())
                .build();
    }

    private void mettreAJourStocks(List<ElementCommande> elements) {
        for (ElementCommande element : elements) {
            Produit produit = element.getProduit();
            int nouvelleQuantite = produit.getQuantiteDisponible() - element.getQuantite();
            produit.setQuantiteDisponible(Math.max(0, nouvelleQuantite));
            produitRepository.save(produit);
        }
    }

    private void restaurerStocks(List<ElementCommande> elements) {
        for (ElementCommande element : elements) {
            Produit produit = element.getProduit();
            int nouvelleQuantite = produit.getQuantiteDisponible() + element.getQuantite();
            produit.setQuantiteDisponible(nouvelleQuantite);
            produitRepository.save(produit);
        }
    }

    private LocalDateTime calculerDateDebut(String periode) {
        LocalDateTime maintenant = LocalDateTime.now();
        return switch (periode.toLowerCase()) {
            case "7j" -> maintenant.minusDays(7);
            case "30j" -> maintenant.minusDays(30);
            case "90j" -> maintenant.minusDays(90);
            case "1an" -> maintenant.minusYears(1);
            default -> maintenant.minusDays(30);
        };
    }

    private CommandeResponse mapToCommandeResponse(Commande commande) {
        return CommandeResponse.builder()
                .id(commande.getId())
                .numeroCommande(commande.getNumeroCommande())
                .acheteurId(commande.getAcheteur().getId())
                .nomAcheteur(commande.getAcheteur().getNom())
                .statut(commande.getStatut())
                .methodePaiement(commande.getMethodePaiement())
                .sousTotal(commande.getSousTotal())
                .fraisLivraison(commande.getFraisLivraison())
                .remise(commande.getRemise())
                .total(commande.getTotal())
                .codePromo(commande.getCodePromo())
                .adresseLivraison(commande.getAdresseLivraison())
                .villeLivraison(commande.getVilleLivraison())
                .codePostalLivraison(commande.getCodePostalLivraison())
                .paysLivraison(commande.getPaysLivraison())
                .telephoneLivraison(commande.getTelephoneLivraison())
                .instructionsLivraison(commande.getInstructionsLivraison())
                .dateCreation(commande.getDateCreation())
                .dateModification(commande.getDateModification())
                .dateLivraisonEstimee(commande.getDateLivraisonEstimee())
                .dateLivraisonEffective(commande.getDateLivraisonEffective())
                .nombreElements(commande.getNombreElements())
                .elements(commande.getElements().stream()
                        .map(this::mapToElementCommandeResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private CommandeResponse.ElementCommandeResponse mapToElementCommandeResponse(ElementCommande element) {
        return CommandeResponse.ElementCommandeResponse.builder()
                .id(element.getId())
                .produitId(element.getProduit().getId())
                .nomProduit(element.getProduit().getNom())
                .imagePrincipale(element.getProduit().getImagePrincipale())
                .prixUnitaire(element.getPrixUnitaire())
                .quantite(element.getQuantite())
                .prixTotal(element.getPrixTotal())
                .unite(element.getProduit().getUnite())
                .build();
    }
} 