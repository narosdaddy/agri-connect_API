package com.cybernerd.agriConnect_APIBackend.serviceImpl;

import com.cybernerd.agriConnect_APIBackend.service.AdminService;
import org.springframework.stereotype.Service;
import com.cybernerd.agriConnect_APIBackend.repository.UtilisateurRepository;
import com.cybernerd.agriConnect_APIBackend.model.Utilisateur;
import lombok.RequiredArgsConstructor;
import java.util.*;
import com.cybernerd.agriConnect_APIBackend.repository.ProduitRepository;
import com.cybernerd.agriConnect_APIBackend.model.Produit;
import com.cybernerd.agriConnect_APIBackend.enumType.StatutModerationProduit;
import com.cybernerd.agriConnect_APIBackend.repository.CommandeRepository;
import com.cybernerd.agriConnect_APIBackend.model.Commande;
import com.cybernerd.agriConnect_APIBackend.repository.EvaluationRepository;
import com.cybernerd.agriConnect_APIBackend.model.Evaluation;
import com.cybernerd.agriConnect_APIBackend.model.CategorieProduit;
import com.cybernerd.agriConnect_APIBackend.repository.CategorieProduitRepository;
import com.cybernerd.agriConnect_APIBackend.service.NotificationService;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UtilisateurRepository utilisateurRepository;
    private final ProduitRepository produitRepository;
    private final CommandeRepository commandeRepository;
    private final EvaluationRepository evaluationRepository;
    private final CategorieProduitRepository categorieProduitRepository;
    private final NotificationService notificationService;

    // Gestion utilisateurs
    @Override
    public Object listUsers() {
        List<Utilisateur> users = utilisateurRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Utilisateur u : users) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", u.getId());
            userMap.put("nom", u.getNom());
            userMap.put("email", u.getEmail());
            userMap.put("role", u.getRole());
            userMap.put("actif", u.isActif());
            result.add(userMap);
        }
        return result;
    }
    @Override
    public Object updateUserStatus(String id, boolean actif) {
        Optional<Utilisateur> userOpt = utilisateurRepository.findById(UUID.fromString(id));
        if (userOpt.isEmpty()) throw new RuntimeException("Utilisateur non trouvé");
        Utilisateur user = userOpt.get();
        user.setActif(actif);
        utilisateurRepository.save(user);
        return Map.of("id", user.getId(), "actif", user.isActif());
    }
    @Override
    public Object deleteUser(String id) {
        Optional<Utilisateur> userOpt = utilisateurRepository.findById(UUID.fromString(id));
        if (userOpt.isEmpty()) throw new RuntimeException("Utilisateur non trouvé");
        utilisateurRepository.deleteById(UUID.fromString(id));
        return Map.of("id", id, "deleted", true);
    }

    // Modération produits
    @Override
    public Object listPendingProducts() {
        List<Produit> produits = produitRepository.findByStatutModeration(StatutModerationProduit.EN_ATTENTE);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Produit p : produits) {
            Map<String, Object> prodMap = new HashMap<>();
            prodMap.put("id", p.getId());
            prodMap.put("nom", p.getNom());
            prodMap.put("statutModeration", p.getStatutModeration());
            prodMap.put("disponible", p.isDisponible());
            result.add(prodMap);
        }
        return result;
    }
    @Override
    public Object validateProduct(String id, boolean valide) {
        Optional<Produit> prodOpt = produitRepository.findById(UUID.fromString(id));
        if (prodOpt.isEmpty()) throw new RuntimeException("Produit non trouvé");
        Produit produit = prodOpt.get();
        produit.setStatutModeration(valide ? StatutModerationProduit.VALIDE : StatutModerationProduit.REFUSE);
        produitRepository.save(produit);
        // Notifier le producteur
        notificationService.notifier(
            produit.getProducteur(),
            valide ? "Votre produit '" + produit.getNom() + "' a été validé et est désormais en ligne." :
                    "Votre produit '" + produit.getNom() + "' a été refusé par l'administration.",
            valide ? "PRODUIT_VALIDE" : "PRODUIT_REFUSE"
        );
        return Map.of("id", produit.getId(), "statutModeration", produit.getStatutModeration());
    }
    @Override
    public Object deleteProduct(String id) {
        Optional<Produit> prodOpt = produitRepository.findById(UUID.fromString(id));
        if (prodOpt.isEmpty()) throw new RuntimeException("Produit non trouvé");
        produitRepository.deleteById(UUID.fromString(id));
        return Map.of("id", id, "deleted", true);
    }

    // Gestion commandes/litiges
    @Override
    public Object listAllOrders() {
        List<Commande> commandes = commandeRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Commande c : commandes) {
            Map<String, Object> cmdMap = new HashMap<>();
            cmdMap.put("id", c.getId());
            cmdMap.put("numeroCommande", c.getNumeroCommande());
            cmdMap.put("acheteur", c.getAcheteur() != null ? c.getAcheteur().getNom() : null);
            cmdMap.put("statut", c.getStatut());
            cmdMap.put("total", c.getTotal());
            cmdMap.put("dateCreation", c.getDateCreation());
            result.add(cmdMap);
        }
        return result;
    }
    @Override
    public Object handleDispute() { return null; }

    // Statistiques/rapports
    @Override
    public Object getReports() {
        long nbUsers = utilisateurRepository.count();
        long nbProducteurs = utilisateurRepository.findAllByRole(com.cybernerd.agriConnect_APIBackend.enumType.Role.PRODUCTEUR).size();
        long nbAcheteurs = utilisateurRepository.findAllByRole(com.cybernerd.agriConnect_APIBackend.enumType.Role.ACHETEUR).size();
        long nbAdmins = utilisateurRepository.findAllByRole(com.cybernerd.agriConnect_APIBackend.enumType.Role.ADMIN).size();
        long nbProduits = produitRepository.count();
        long nbCommandes = commandeRepository.count();
        return Map.of(
            "utilisateurs", nbUsers,
            "producteurs", nbProducteurs,
            "acheteurs", nbAcheteurs,
            "admins", nbAdmins,
            "produits", nbProduits,
            "commandes", nbCommandes
        );
    }

    // Gestion avis
    @Override
    public Object listReviews() {
        List<Evaluation> avis = evaluationRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Evaluation e : avis) {
            Map<String, Object> avisMap = new HashMap<>();
            avisMap.put("id", e.getId());
            avisMap.put("produit", e.getProduit() != null ? e.getProduit().getNom() : null);
            avisMap.put("acheteur", e.getAcheteur() != null ? e.getAcheteur().getNom() : null);
            avisMap.put("note", e.getNote());
            avisMap.put("commentaire", e.getCommentaire());
            avisMap.put("date", e.getDateCreation());
            result.add(avisMap);
        }
        return result;
    }
    @Override
    public Object deleteReview(String id) {
        Optional<Evaluation> avisOpt = evaluationRepository.findById(UUID.fromString(id));
        if (avisOpt.isEmpty()) throw new RuntimeException("Avis non trouvé");
        evaluationRepository.deleteById(UUID.fromString(id));
        return Map.of("id", id, "deleted", true);
    }

    // Gestion catégories
    @Override
    public Object listCategories() {
        List<CategorieProduit> categories = categorieProduitRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (CategorieProduit c : categories) {
            Map<String, Object> catMap = new HashMap<>();
            catMap.put("id", c.getId());
            catMap.put("nom", c.getNom());
            catMap.put("description", c.getDescription());
            result.add(catMap);
        }
        return result;
    }
    @Override
    public Object addCategory(String nom, String description) {
        if (categorieProduitRepository.findByNom(nom).isPresent()) throw new RuntimeException("Nom de catégorie déjà utilisé");
        CategorieProduit cat = CategorieProduit.builder().nom(nom).description(description).build();
        categorieProduitRepository.save(cat);
        return Map.of("id", cat.getId(), "nom", cat.getNom());
    }
    @Override
    public Object updateCategory(String id, String nom, String description) {
        CategorieProduit cat = categorieProduitRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
        cat.setNom(nom);
        cat.setDescription(description);
        categorieProduitRepository.save(cat);
        return Map.of("id", cat.getId(), "nom", cat.getNom());
    }
    @Override
    public Object deleteCategory(String id) {
        UUID catId = UUID.fromString(id);
        if (!categorieProduitRepository.existsById(catId)) throw new RuntimeException("Catégorie non trouvée");
        long nbProduits = produitRepository.findAll().stream().filter(p -> p.getCategorie() != null && catId.equals(p.getCategorie().getId())).count();
        if (nbProduits > 0) throw new RuntimeException("Impossible de supprimer : catégorie utilisée par des produits.");
        categorieProduitRepository.deleteById(catId);
        return Map.of("id", id, "deleted", true);
    }

    @Override
    public Utilisateur getUtilisateurById(UUID utilisateurId) {
        return utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
} 