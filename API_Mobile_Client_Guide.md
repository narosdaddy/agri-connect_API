# Guide d'intégration API pour le client mobile AgriConnect

## 1. Présentation du projet

**AgriConnect** est une plateforme facilitant la mise en relation entre producteurs agricoles, acheteurs, partenaires logistiques et administrateurs. L'API backend est RESTful, sécurisée par JWT, et propose des fonctionnalités d'authentification, gestion de produits, commandes, panier, paiements, notifications, etc.

---

## 2. Description métier des acteurs

### Acheteur (Client)
- Parcourt le catalogue, recherche des produits, consulte les détails.
- Ajoute des produits à son panier, applique des codes promo.
- Passe commande, choisit la livraison, suit le statut de ses commandes.
- Reçoit des notifications (commande confirmée, expédiée, etc.).
- Peut évaluer les produits et producteurs.
- Peut demander à devenir producteur (envoi de documents justificatifs).

### Producteur
- Gère son catalogue de produits (création, modification, suppression, gestion du stock).
- Suit les commandes reçues, met à jour leur statut (préparation, livraison, etc.).
- Reçoit des notifications (nouvelles commandes, avis, etc.).
- Peut voir ses statistiques de ventes, évaluations, chiffre d'affaires.
- Peut être certifié bio et vérifié par l'admin.

#### Statuts de profil
- `BASIQUE` : Acheteur classique
- `DEMANDE_PRODUCTEUR` : Demande en cours pour devenir producteur
- `PRODUCTEUR_VERIFIE` : Producteur validé
- `PRODUCTEUR_REFUSE` : Demande refusée

---

## 3. Authentification

### Endpoints principaux
- `POST /api/v1/auth/register` — Inscription
- `POST /api/v1/auth/login` — Connexion
- `GET /api/v1/auth/verify?token=...` — Vérification email
- `POST /api/v1/auth/resend-verification?email=...` — Renvoyer email de vérification
- `POST /api/v1/auth/refresh-token?token=...` — Rafraîchir le JWT
- `POST /api/v1/auth/reset-password?token=...&newPassword=...` — Réinitialiser le mot de passe
- `POST /api/v1/auth/forgot-password?email=...` — Demande de reset
- `GET /api/v1/auth/email-verified?email=...` — Statut email vérifié
- `POST /api/v1/auth/logout?token=...` — Déconnexion

### Exemples de payload
**Inscription**
```json
{
  "nom": "Jean Dupont",
  "email": "jean.dupont@email.com",
  "telephone": "+33 6 12 34 56 78",
  "motDePasse": "motdepasse123"
}
```
**Connexion**
```json
{
  "email": "jean.dupont@email.com",
  "motDePasse": "motdepasse123"
}
```
**Réponse d'authentification**
```json
{
  "id": "uuid",
  "nom": "Jean Dupont",
  "email": "jean.dupont@email.com",
  "telephone": "+33 6 12 34 56 78",
  "role": "ACHETEUR",
  "token": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "type": "Bearer",
  "emailVerifie": true,
  "adresse": "123 Rue de la Paix",
  "ville": "Paris",
  "codePostal": "75001",
  "pays": "France",
  "avatar": "https://...",
  "nomExploitation": "...",
  "descriptionExploitation": "...",
  "certifieBio": false,
  "verifie": false
}
```

---

## 4. Gestion du profil utilisateur

### Demande d'évolution vers producteur
- `POST /api/v1/profil/demande-producteur` (multipart/form-data)
  - `nomExploitation` (string)
  - `descriptionExploitation` (string)
  - `adresseExploitation` (string)
  - `telephoneExploitation` (string)
  - `certificatBio` (fichier, optionnel)
  - `documentIdentite` (fichier, requis)
  - `justificatifAdresse` (fichier, requis)
  - `autresDocuments` (fichiers, optionnel)

**Réponse**
```json
{"message": "Demande d'évolution vers producteur soumise avec succès"}
```

---

## 5. Notifications

### Endpoints principaux
- `GET /notifications` — Lister les notifications de l'utilisateur connecté
- `POST /notifications/{id}/read` — Marquer une notification comme lue
- `POST /notifications/read-all` — Tout marquer comme lu

**Exemple de notification**
```json
{
  "id": "uuid",
  "message": "Votre commande #CMD-2024-001 a été confirmée",
  "type": "COMMANDE_CONFIRMEE",
  "lu": false,
  "dateCreation": "2024-06-25T12:00:00"
}
```

---

## 6. Gestion des produits

### Endpoints principaux
- `POST /api/v1/produits` — Créer un produit (Producteur)
- `PUT /api/v1/produits/{produitId}` — Modifier un produit (Producteur)
- `DELETE /api/v1/produits/{produitId}` — Supprimer un produit (Producteur)
- `GET /api/v1/produits/{produitId}` — Détail d'un produit
- `GET /api/v1/produits` — Liste paginée des produits
- `GET /api/v1/produits/search` — Recherche/filtres
- `GET /api/v1/produits/producteur/{producteurId}` — Produits d'un producteur
- `GET /api/v1/produits/popular` — Produits populaires
- `GET /api/v1/produits/recent` — Produits récents
- `POST /api/v1/produits/{produitId}/images` — Upload image (multipart)
- `DELETE /api/v1/produits/{produitId}/images` — Supprimer image

### Exemple de création de produit
```json
{
  "nom": "Tomates cerises bio",
  "description": "Tomates cerises cultivées en agriculture biologique...",
  "prix": 4.50,
  "quantiteDisponible": 100,
  "categorieId": "LEGUMES",
  "unite": "kg",
  "bio": true,
  "origine": "France",
  "imagePrincipale": "https://...",
  "images": ["https://..."]
}
```

---

## 7. Commandes

### Endpoints principaux
- `POST /orders` — Créer une commande (Acheteur)
- `GET /orders/{commandeId}` — Détail d'une commande
- `GET /orders/acheteur/{acheteurId}` — Commandes d'un acheteur
- `GET /orders/producteur/{producteurId}` — Commandes d'un producteur
- `PUT /orders/{commandeId}/status` — Changer statut (Producteur)
- `POST /orders/{commandeId}/cancel` — Annuler (Acheteur)
- `GET /orders/recent` — Commandes récentes

### Exemple de création de commande
```json
{
  "adresseLivraison": "123 Rue de la Paix",
  "villeLivraison": "Paris",
  "codePostalLivraison": "75001",
  "paysLivraison": "France",
  "telephoneLivraison": "+33123456789",
  "instructionsLivraison": "Livrer entre 14h et 18h",
  "methodePaiement": "CARTE_BANCAIRE",
  "codePromo": "PROMO10",
  "elements": [
    {
      "produitId": "uuid-produit",
      "quantite": 2,
      "prixUnitaire": 4.5
    }
  ],
  "souhaiteLivraison": true,
  "partenaireLogistiqueId": "uuid-partenaire"
}
```

---

## 8. Panier

### Endpoints principaux
- `POST /api/v1/cart` — Ajouter au panier
- `GET /api/v1/cart/{acheteurId}` — Voir le panier
- `DELETE /api/v1/cart/{acheteurId}/products/{produitId}` — Retirer un produit
- `DELETE /api/v1/cart/{acheteurId}` — Vider le panier

### Exemple d'ajout au panier
```json
{
  "produitId": "uuid-produit",
  "quantite": 2
}
```

---

## 9. Paiement

### Endpoints principaux
- `POST /api/v1/paiements` — Créer un paiement
- `GET /api/v1/paiements/{id}` — Détail d'un paiement
- `GET /api/v1/paiements` — Liste des paiements

---

## 10. Rôles utilisateurs

- `ACHETEUR` : Achat, gestion panier, commandes
- `PRODUCTEUR` : Gestion produits, suivi commandes
- `ADMIN` : Modération, gestion catalogue
- `SUPER_ADMIN` : Gestion utilisateurs/admins
- `PARTENAIRE_LOGISTIQUE` : Livraison

---

## 11. Statuts de commande

- `EN_ATTENTE`
- `EN_COURS`
- `CONFIRMEE`
- `EN_PREPARATION`
- `EN_LIVRAISON`
- `LIVREE`
- `ANNULEE`

---

## 12. Méthodes de paiement

- `CARTE_BANCAIRE`
- `MOBILE_MONEY`
- `VIREMENT_BANCAIRE`
- `ESPECES_LIVRAISON`
- `PAYPAL`
- `STRIPE`

---

## 13. Gestion des erreurs

- Les erreurs sont généralement retournées avec un code HTTP approprié (`400`, `401`, `403`, `404`, `409`, etc.) et un message dans le corps de la réponse :
```json
{
  "timestamp": "2024-06-25T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Le panier est vide",
  "path": "/orders"
}
```
- Les messages d'erreur sont en français.

---

## 14. Sécurité & Authentification

- Authentification par JWT (header `Authorization: Bearer <token>`)
- Rafraîchissement du token via `/api/v1/auth/refresh-token`
- CORS activé par défaut (vérifier la config si besoin d'ajuster pour le mobile)

---

## 15. Points d'attention

- **Upload d'images** : multipart/form-data, endpoint dédié
- **Pagination** : paramètres `page`, `size` sur les endpoints listant des ressources
- **Tri** : paramètres `sortBy`, `sortDir` sur certains endpoints
- **Gestion des rôles** : certains endpoints nécessitent un rôle spécifique (`@PreAuthorize`)
- **Format UUID** : utilisé pour la plupart des identifiants
