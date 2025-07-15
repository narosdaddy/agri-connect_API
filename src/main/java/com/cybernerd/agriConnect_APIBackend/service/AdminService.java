package com.cybernerd.agriConnect_APIBackend.service;

import com.cybernerd.agriConnect_APIBackend.model.Utilisateur;
import java.util.UUID;

public interface AdminService {
    // Gestion utilisateurs
    Object listUsers();
    Object updateUserStatus(String id, boolean actif);
    Object deleteUser(String id);

    // Modération produits
    Object listPendingProducts();
    Object validateProduct(String id, boolean valide);
    Object deleteProduct(String id);

    // Gestion commandes/litiges
    Object listAllOrders();
    Object handleDispute();

    // Statistiques/rapports
    Object getReports();

    // Gestion avis
    Object listReviews();
    Object deleteReview(String id);

    // Gestion catégories
    Object listCategories();
    Object addCategory(String nom, String description);
    Object updateCategory(String id, String nom, String description);
    Object deleteCategory(String id);

    Utilisateur getUtilisateurById(UUID utilisateurId);
} 