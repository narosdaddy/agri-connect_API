# Documentation API AgriConnect

> **Toutes les routes nécessitant un token JWT attendent le header :**
> 
>     Authorization: Bearer <token>

---

## Sommaire
- [Authentification](#authentification)
- [Profil](#profil)
- [Produits](#produits)
- [Commandes](#commandes)
- [Panier](#panier)
- [Paiement](#paiement)
- [Notifications](#notifications)
- [Livraisons](#livraisons)
- [Partenaires logistiques](#partenaires-logistiques)
- [Administration](#administration)
- [SuperAdmin](#superadmin)

---

## Authentification

### POST /auth/login
- **Description** : Authentifie un utilisateur avec email et mot de passe.
- **Corps requis** :
```json
{
  "email": "user@example.com",
  "motDePasse": "string"
}
```
- **Exemple de réponse** :
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "nom": "Jean Dupont",
  "email": "user@example.com",
  "telephone": "+33 6 12 34 56 78",
  "role": "ACHETEUR",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "",
  "type": "Bearer",
  "emailVerifie": true,
  "adresse": "123 Rue de la Paix",
  "ville": "Paris",
  "codePostal": "75001",
  "pays": "France",
  "avatar": null,
  "nomExploitation": null,
  "descriptionExploitation": null,
  "certifieBio": false,
  "verifie": false
}
```
- **Codes de réponse** :
  - 200 : Connexion réussie
  - 401 : Identifiants invalides
  - 403 : Email non vérifié

---

### POST /auth/register
- **Description** : Inscription d'un nouvel utilisateur.
- **Corps requis** :
```json
{
  "nom": "Jean Dupont",
  "email": "user@example.com",
  "motDePasse": "string",
  "telephone": "+33 6 12 34 56 78"
}
```
- **Exemple de réponse** :
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "nom": "Jean Dupont",
  "email": "user@example.com",
  "telephone": "+33 6 12 34 56 78",
  "role": "ACHETEUR",
  "emailVerifie": false
}
```
- **Codes de réponse** :
  - 201 : Utilisateur enregistré avec succès
  - 400 : Données invalides
  - 409 : Email déjà utilisé

---

### POST /auth/verify
- **Description** : Vérifie le code de validation envoyé par email.
- **Corps requis** :
```json
{
  "code": "123456"
}
```
- **Exemple de réponse** :
```json
"Email vérifié avec succès"
```
- **Codes de réponse** :
  - 200 : Email vérifié avec succès
  - 400 : Code invalide ou expiré

---

### POST /auth/refresh-token
- **Description** : Rafraîchit le token JWT d'un utilisateur.
- **Paramètres** :
  - `token` (string, query param)
- **Exemple de réponse** :
```json
{
  "id": "...",
  "nom": "...",
  "email": "...",
  "token": "nouveau_token_jwt",
  ...
}
```
- **Codes de réponse** :
  - 200 : Token rafraîchi
  - 401 : Token invalide

---

### GET /auth/email-verified
- **Description** : Vérifie si l'email est validé.
- **Paramètres** :
  - `email` (string, query param)
- **Exemple de réponse** :
```json
true
```
- **Codes de réponse** :
  - 200 : Statut de vérification retourné

---

### POST /auth/forgot-password
- **Description** : Envoie un email de réinitialisation de mot de passe.
- **Paramètres** :
  - `email` (string, query param)
- **Exemple de réponse** :
```json
"Email de réinitialisation envoyé"
```
- **Codes de réponse** :
  - 200 : Email envoyé
  - 404 : Utilisateur non trouvé

---

### POST /auth/resend-verification
- **Description** : Renvoyer l'email de vérification.
- **Paramètres** :
  - `email` (string, query param)
- **Exemple de réponse** :
```json
"Email de vérification envoyé"
```
- **Codes de réponse** :
  - 200 : Email envoyé
  - 404 : Utilisateur non trouvé

---

## Profil

### GET /api/v1/profil/me
- **Description** : Récupère le profil de l'utilisateur connecté (JWT requis).
- **Headers** :
  - `Authorization: Bearer <token>`
- **Exemple de réponse** :
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "nom": "Jean Dupont",
  "email": "user@example.com",
  "role": "ACHETEUR",
  ...
}
```
- **Codes de réponse** :
  - 200 : Profil récupéré
  - 401 : Non authentifié

---

### POST /api/v1/profil/demande-producteur
- **Description** : Demande d'évolution de profil vers producteur (multipart/form-data, JWT requis).
- **Corps requis** : `multipart/form-data` (voir modèle `EvolutionProducteurRequest`)
- **Exemple de réponse** :
```json
"Demande d'évolution vers producteur soumise avec succès"
```
- **Codes de réponse** :
  - 200 : Demande soumise

---

## Produits

### POST /api/v1/produits
- **Description** : Créer un nouveau produit (Producteur uniquement).
- **Corps requis** :
```json
{
  "nom": "Tomates Bio",
  "description": "Tomates fraîches bio du jardin",
  "prix": 2.5,
  "categorieId": "...",
  "quantite": 100,
  ...
}
```
- **Paramètres** :
  - `producteurId` (UUID, query param)
- **Exemple de réponse** :
```json
{
  "id": "...",
  "nom": "Tomates Bio",
  ...
}
```
- **Codes de réponse** :
  - 201 : Produit créé
  - 400 : Données invalides
  - 403 : Accès refusé

### PUT /api/v1/produits/{produitId}
- **Description** : Modifier un produit (Producteur uniquement).
- **Corps requis** : (identique à création)
- **Paramètres** :
  - `produitId` (UUID, path)
  - `producteurId` (UUID, query param)
- **Exemple de réponse** :
```json
{
  "id": "...",
  "nom": "Tomates Bio",
  ...
}
```
- **Codes de réponse** :
  - 200 : Produit modifié
  - 400 : Données invalides
  - 403 : Accès refusé
  - 404 : Produit non trouvé

### DELETE /api/v1/produits/{produitId}
- **Description** : Supprimer un produit (Producteur uniquement).
- **Paramètres** :
  - `produitId` (UUID, path)
  - `producteurId` (UUID, query param)
- **Codes de réponse** :
  - 204 : Produit supprimé
  - 403 : Accès refusé
  - 404 : Produit non trouvé

### GET /api/v1/produits/{produitId}
- **Description** : Obtenir un produit par ID.
- **Paramètres** :
  - `produitId` (UUID, path)
- **Exemple de réponse** :
```json
{
  "id": "...",
  "nom": "Tomates Bio",
  ...
}
```
- **Codes de réponse** :
  - 200 : Produit trouvé
  - 404 : Produit non trouvé

### GET /api/v1/produits
- **Description** : Obtenir tous les produits (pagination).
- **Paramètres** :
  - `page` (int, query, default 0)
  - `size` (int, query, default 20)
  - `sortBy` (string, query, default "dateCreation")
  - `sortDir` (string, query, default "DESC")
- **Exemple de réponse** :
```json
{
  "content": [ { "id": "...", "nom": "..." } ],
  "totalElements": 100,
  ...
}
```
- **Codes de réponse** :
  - 200 : Liste des produits

### GET /api/v1/produits/search
- **Description** : Rechercher des produits avec filtres.
- **Paramètres** :
  - `categorie` (string, query, optionnel)
  - `bio` (bool, query, optionnel)
  - `prixMin` (decimal, query, optionnel)
  - `prixMax` (decimal, query, optionnel)
  - `nom` (string, query, optionnel)
  - `origine` (string, query, optionnel)
  - `page`, `size` (pagination)
- **Exemple de réponse** : (identique à /produits)
- **Codes de réponse** :
  - 200 : Résultats de recherche

### GET /api/v1/produits/producteur/{producteurId}
- **Description** : Obtenir les produits d'un producteur.
- **Paramètres** :
  - `producteurId` (UUID, path)
  - `page`, `size` (pagination)
- **Codes de réponse** :
  - 200 : Produits du producteur

### GET /api/v1/produits/popular
- **Description** : Obtenir les produits populaires.
- **Paramètres** :
  - `page`, `size` (pagination)
- **Codes de réponse** :
  - 200 : Produits populaires

### GET /api/v1/produits/recent
- **Description** : Obtenir les produits récents.
- **Paramètres** :
  - `page`, `size` (pagination)
- **Codes de réponse** :
  - 200 : Produits récents

### GET /api/v1/produits/out-of-stock
- **Description** : Obtenir les produits en rupture de stock (Producteur uniquement).
- **Codes de réponse** :
  - 200 : Produits en rupture

### POST /api/v1/produits/{produitId}/images
- **Description** : Uploader une image de produit (Producteur uniquement).
- **Corps** : `multipart/form-data` (champ `file`)
- **Paramètres** :
  - `produitId` (UUID, path)
  - `producteurId` (UUID, query param)
- **Exemple de réponse** :
```json
"https://.../image.jpg"
```
- **Codes de réponse** :
  - 200 : Image uploadée
  - 400 : Fichier invalide
  - 403 : Accès refusé

### DELETE /api/v1/produits/{produitId}/images
- **Description** : Supprimer une image de produit (Producteur uniquement).
- **Paramètres** :
  - `produitId` (UUID, path)
  - `imageUrl` (string, query param)
  - `producteurId` (UUID, query param)
- **Codes de réponse** :
  - 204 : Image supprimée
  - 403 : Accès refusé
  - 404 : Image non trouvée

### PATCH /api/v1/produits/{produitId}/stock
- **Description** : Mettre à jour le stock d'un produit (Producteur uniquement).
- **Paramètres** :
  - `produitId` (UUID, path)
  - `nouvelleQuantite` (int, query param)
  - `producteurId` (UUID, query param)
- **Codes de réponse** :
  - 200 : Stock mis à jour
  - 400 : Quantité invalide
  - 403 : Accès refusé
  - 404 : Produit non trouvé

### GET /api/v1/produits/{produitId}/availability
- **Description** : Vérifier la disponibilité d'un produit.
- **Paramètres** :
  - `produitId` (UUID, path)
  - `quantiteDemandee` (int, query param)
- **Exemple de réponse** :
```json
true
```
- **Codes de réponse** :
  - 200 : Disponibilité vérifiée

---

## Commandes

### POST /orders
- **Description** : Créer une nouvelle commande (Acheteur uniquement).
- **Corps requis** :
```json
{
  "elements": [
    { "produitId": "...", "quantite": 2 }
  ],
  ...
}
```
- **Paramètres** :
  - `acheteurId` (UUID, query param)
- **Exemple de réponse** :
```json
{
  "id": "...",
  "statut": "EN_COURS",
  ...
}
```
- **Codes de réponse** :
  - 201 : Commande créée
  - 400 : Données invalides
  - 401 : Non autorisé
  - 404 : Acheteur ou panier non trouvé

### GET /orders/{commandeId}
- **Description** : Récupérer une commande par ID.
- **Paramètres** :
  - `commandeId` (UUID, path)
- **Exemple de réponse** :
```json
{
  "id": "...",
  "statut": "EN_COURS",
  ...
}
```
- **Codes de réponse** :
  - 200 : Commande trouvée
  - 401 : Non autorisé
  - 404 : Commande non trouvée

### GET /orders/acheteur/{acheteurId}
- **Description** : Récupérer les commandes d'un acheteur (pagination).
- **Paramètres** :
  - `acheteurId` (UUID, path)
  - `page`, `size` (pagination)
- **Codes de réponse** :
  - 200 : Commandes récupérées

### GET /orders/producteur/{producteurId}
- **Description** : Récupérer les commandes d'un producteur (pagination).
- **Paramètres** :
  - `producteurId` (UUID, path)
  - `page`, `size` (pagination)
- **Codes de réponse** :
  - 200 : Commandes récupérées

### PUT /orders/{commandeId}/status
- **Description** : Mettre à jour le statut d'une commande (Producteur uniquement).
- **Paramètres** :
  - `commandeId` (UUID, path)
  - `nouveauStatut` (enum, query param)
  - `producteurId` (UUID, query param)
- **Exemple de réponse** :
```json
{
  "id": "...",
  "statut": "LIVREE",
  ...
}
```
- **Codes de réponse** :
  - 200 : Statut mis à jour
  - 400 : Statut invalide
  - 401 : Non autorisé
  - 404 : Commande non trouvée

### GET /orders/search
- **Description** : Rechercher des commandes avec filtres.
- **Paramètres** :
  - `statut` (enum, query param, optionnel)
  - `numeroCommande` (string, query param, optionnel)
  - `acheteurId` (UUID, query param, optionnel)
  - `producteurId` (UUID, query param, optionnel)
  - `page`, `size` (pagination)
- **Codes de réponse** :
  - 200 : Recherche effectuée
  - 400 : Paramètres invalides

### GET /orders/analytics/producteur/{producteurId}
- **Description** : Analytics commandes pour un producteur.
- **Paramètres** :
  - `producteurId` (UUID, path)
  - `periode` (string, query param, ex: 7j, 30j, 90j, 1an)
- **Codes de réponse** :
  - 200 : Analytics récupérées

### POST /orders/{commandeId}/cancel
- **Description** : Annuler une commande (Acheteur uniquement).
- **Paramètres** :
  - `commandeId` (UUID, path)
  - `acheteurId` (UUID, query param)
- **Codes de réponse** :
  - 200 : Commande annulée
  - 400 : Non annulable
  - 401 : Non autorisé
  - 404 : Commande non trouvée

### GET /orders/recent
- **Description** : Récupérer les commandes récentes (pagination).
- **Paramètres** :
  - `page`, `size` (pagination)
- **Codes de réponse** :
  - 200 : Commandes récentes récupérées

---

## Panier

### POST /api/v1/cart
- **Description** : Ajouter un produit au panier (Acheteur).
- **Corps requis** :
```json
{
  "produitId": "...",
  "quantite": 2
}
```
- **Paramètres** :
  - `acheteurId` (UUID, query param)
- **Exemple de réponse** :
```json
{
  "id": "...",
  "elements": [ { "produitId": "...", "quantite": 2 } ],
  ...
}
```
- **Codes de réponse** :
  - 201 : Produit ajouté
  - 400 : Données invalides

### GET /api/v1/cart/{acheteurId}
- **Description** : Récupérer le panier d'un acheteur.
- **Paramètres** :
  - `acheteurId` (UUID, path)
- **Exemple de réponse** :
```json
{
  "id": "...",
  "elements": [ { "produitId": "...", "quantite": 2 } ],
  ...
}
```
- **Codes de réponse** :
  - 200 : Panier récupéré

### DELETE /api/v1/cart/{acheteurId}/products/{produitId}
- **Description** : Supprimer un produit du panier.
- **Paramètres** :
  - `acheteurId` (UUID, path)
  - `produitId` (UUID, path)
- **Codes de réponse** :
  - 200 : Produit supprimé

### DELETE /api/v1/cart/{acheteurId}
- **Description** : Vider le panier.
- **Paramètres** :
  - `acheteurId` (UUID, path)
- **Codes de réponse** :
  - 200 : Panier vidé

---

## Paiement

### POST /api/v1/paiements
- **Description** : Créer un paiement pour une commande.
- **Corps requis** :
```json
{
  "commandeId": "...",
  "montant": 50.0,
  "methode": "STRIPE"
}
```
- **Exemple de réponse** :
```json
{
  "id": "...",
  "statut": "SUCCES",
  ...
}
```
- **Codes de réponse** :
  - 200 : Paiement créé

### GET /api/v1/paiements/{id}
- **Description** : Récupérer un paiement par ID.
- **Paramètres** :
  - `id` (UUID, path)
- **Exemple de réponse** :
```json
{
  "id": "...",
  "statut": "SUCCES",
  ...
}
```
- **Codes de réponse** :
  - 200 : Paiement trouvé

### GET /api/v1/paiements
- **Description** : Lister tous les paiements.
- **Exemple de réponse** :
```json
[
  { "id": "...", "statut": "SUCCES" },
  ...
]
```
- **Codes de réponse** :
  - 200 : Liste des paiements

---

## Notifications

### GET /notifications
- **Description** : Lister les notifications de l'utilisateur connecté.
- **Exemple de réponse** :
```json
[
  { "id": "...", "message": "...", "lue": false },
  ...
]
```
- **Codes de réponse** :
  - 200 : Notifications récupérées
  - 401 : Non autorisé

### POST /notifications/{id}/read
- **Description** : Marquer une notification comme lue.
- **Paramètres** :
  - `id` (UUID, path)
- **Codes de réponse** :
  - 200 : Notification marquée comme lue
  - 401 : Non autorisé
  - 404 : Notification non trouvée

### POST /notifications/read-all
- **Description** : Tout marquer comme lu.
- **Codes de réponse** :
  - 200 : Toutes les notifications marquées comme lues
  - 401 : Non autorisé

---

## Livraisons

### POST /api/v1/livraisons
- **Description** : Créer une livraison pour une commande.
- **Corps requis** :
```json
{
  "commandeId": "...",
  "partenaireId": "...",
  ...
}
```
- **Exemple de réponse** :
```json
{
  "id": "...",
  "statut": "EN_COURS",
  ...
}
```
- **Codes de réponse** :
  - 200 : Livraison créée

### GET /api/v1/livraisons/{id}
- **Description** : Récupérer une livraison par ID.
- **Paramètres** :
  - `id` (UUID, path)
- **Codes de réponse** :
  - 200 : Livraison trouvée

### GET /api/v1/livraisons/commande/{commandeId}
- **Description** : Récupérer les livraisons d'une commande.
- **Paramètres** :
  - `commandeId` (UUID, path)
- **Codes de réponse** :
  - 200 : Livraisons récupérées

### GET /api/v1/livraisons/partenaire/{partenaireId}
- **Description** : Récupérer les livraisons d'un partenaire logistique.
- **Paramètres** :
  - `partenaireId` (UUID, path)
- **Codes de réponse** :
  - 200 : Livraisons récupérées

### PATCH /api/v1/livraisons/{id}/statut
- **Description** : Mettre à jour le statut d'une livraison.
- **Paramètres** :
  - `id` (UUID, path)
  - `statut` (string, query param)
- **Codes de réponse** :
  - 200 : Statut mis à jour

### DELETE /api/v1/livraisons/{id}
- **Description** : Supprimer une livraison.
- **Paramètres** :
  - `id` (UUID, path)
- **Codes de réponse** :
  - 204 : Livraison supprimée

---

## Partenaires logistiques

### POST /api/v1/partenaires-logistiques
- **Description** : Créer un partenaire logistique.
- **Corps requis** :
```json
{
  "nom": "Chronopost",
  ...
}
```
- **Exemple de réponse** :
```json
{
  "id": "...",
  "nom": "Chronopost",
  ...
}
```
- **Codes de réponse** :
  - 200 : Partenaire créé

### GET /api/v1/partenaires-logistiques/{id}
- **Description** : Récupérer un partenaire logistique par ID.
- **Paramètres** :
  - `id` (UUID, path)
- **Codes de réponse** :
  - 200 : Partenaire trouvé

### GET /api/v1/partenaires-logistiques
- **Description** : Lister tous les partenaires logistiques.
- **Codes de réponse** :
  - 200 : Liste des partenaires

### PATCH /api/v1/partenaires-logistiques/{id}/activer
- **Description** : Activer un partenaire logistique.
- **Paramètres** :
  - `id` (UUID, path)
- **Codes de réponse** :
  - 200 : Partenaire activé

### PATCH /api/v1/partenaires-logistiques/{id}/desactiver
- **Description** : Désactiver un partenaire logistique.
- **Paramètres** :
  - `id` (UUID, path)
- **Codes de réponse** :
  - 200 : Partenaire désactivé

### DELETE /api/v1/partenaires-logistiques/{id}
- **Description** : Supprimer un partenaire logistique.
- **Paramètres** :
  - `id` (UUID, path)
- **Codes de réponse** :
  - 204 : Partenaire supprimé

---

## Administration

> **Tous les endpoints /admin nécessitent le rôle ADMIN**

### GET /admin/users
- **Description** : Lister tous les utilisateurs.
- **Codes de réponse** :
  - 200 : Liste des utilisateurs

### PATCH /admin/users/{id}/status
- **Description** : Activer/désactiver un utilisateur.
- **Paramètres** :
  - `id` (string, path)
  - `actif` (bool, query param)
- **Codes de réponse** :
  - 200 : Statut mis à jour

### DELETE /admin/users/{id}
- **Description** : Supprimer un utilisateur.
- **Paramètres** :
  - `id` (string, path)
- **Codes de réponse** :
  - 200 : Utilisateur supprimé

### GET /admin/products/pending
- **Description** : Lister les produits en attente de validation.
- **Codes de réponse** :
  - 200 : Liste des produits en attente

### PATCH /admin/products/{id}/validate
- **Description** : Valider/refuser un produit.
- **Paramètres** :
  - `id` (string, path)
  - `valide` (bool, query param)
- **Codes de réponse** :
  - 200 : Statut du produit mis à jour

### DELETE /admin/products/{id}
- **Description** : Supprimer un produit.
- **Paramètres** :
  - `id` (string, path)
- **Codes de réponse** :
  - 200 : Produit supprimé

### GET /admin/orders
- **Description** : Lister toutes les commandes.
- **Codes de réponse** :
  - 200 : Liste des commandes

### POST /admin/disputes
- **Description** : Gérer un litige.
- **Codes de réponse** :
  - 200 : Litige traité

### GET /admin/reports
- **Description** : Obtenir les rapports/statistiques de la plateforme.
- **Codes de réponse** :
  - 200 : Rapports récupérés

### GET /admin/reviews
- **Description** : Lister tous les avis/évaluations.
- **Codes de réponse** :
  - 200 : Liste des avis

### DELETE /admin/reviews/{id}
- **Description** : Supprimer un avis.
- **Paramètres** :
  - `id` (string, path)
- **Codes de réponse** :
  - 200 : Avis supprimé

### GET /admin/categories
- **Description** : Lister toutes les catégories de produits.
- **Codes de réponse** :
  - 200 : Liste des catégories

### POST /admin/categories
- **Description** : Ajouter une catégorie.
- **Paramètres** :
  - `nom` (string, query param)
  - `description` (string, query param, optionnel)
- **Codes de réponse** :
  - 200 : Catégorie ajoutée

### PATCH /admin/categories/{id}
- **Description** : Modifier une catégorie.
- **Paramètres** :
  - `id` (string, path)
  - `nom` (string, query param)
  - `description` (string, query param, optionnel)
- **Codes de réponse** :
  - 200 : Catégorie modifiée

### DELETE /admin/categories/{id}
- **Description** : Supprimer une catégorie.
- **Paramètres** :
  - `id` (string, path)
- **Codes de réponse** :
  - 200 : Catégorie supprimée

### GET /admin/demandes-producteur
- **Description** : Lister les demandes d'évolution vers producteur.
- **Codes de réponse** :
  - 200 : Liste des demandes

### POST /admin/valider-producteur/{userId}
- **Description** : Valider un profil producteur.
- **Paramètres** :
  - `userId` (UUID, path)
- **Codes de réponse** :
  - 200 : Profil producteur validé

### POST /admin/refuser-producteur/{userId}
- **Description** : Refuser une demande producteur.
- **Paramètres** :
  - `userId` (UUID, path)
- **Codes de réponse** :
  - 200 : Demande producteur refusée

---

## SuperAdmin

### POST /api/v1/superadmin/create-user
- **Description** : Créer un utilisateur avec un rôle spécifique (SUPER_ADMIN uniquement).
- **Corps requis** :
```json
{
  "nom": "...",
  "email": "...",
  "motDePasse": "...",
  "telephone": "..."
}
```
- **Paramètres** :
  - `role` (string, query param)
- **Exemple de réponse** :
```json
"Utilisateur créé avec succès"
```
- **Codes de réponse** :
  - 200 : Utilisateur créé

---

> **Pour chaque entité, adapte les modèles de données à partir des exemples de réponses pour structurer tes models, entities, repositories et usecases côté Flutter.** 