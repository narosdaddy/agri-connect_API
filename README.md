# AgriConnect API - Marketplace Agricole

API REST Spring Boot pour une marketplace agricole connectant acheteurs et producteurs locaux.

## 🚀 Fonctionnalités

### Authentification & Utilisateurs
- ✅ Inscription/Connexion avec rôles (ACHETEUR/PRODUCTEUR)
- ✅ JWT avec refresh token
- ✅ Gestion profil utilisateur
- ✅ Mot de passe oublié/réinitialisation
- ✅ Vérification email

### Gestion des Produits
- ✅ CRUD complet pour les producteurs
- ✅ Recherche et filtres avancés
- ✅ Pagination et tri
- ✅ 10 catégories de produits agricoles
- ✅ Gestion des images
- ✅ Système de notes et avis

### Panier d'Achat
- ✅ Ajout/suppression/modification quantité
- ✅ Codes promo (BIENVENUE10, FRESH20, BIO15)
- ✅ Calcul automatique des totaux
- ✅ Persistance par utilisateur

### Commandes
- ✅ Création depuis le panier
- ✅ 6 statuts de commande
- ✅ Historique des commandes
- ✅ Analytics pour producteurs

### Sécurité
- ✅ Spring Security avec JWT
- ✅ Validation des données
- ✅ CORS configuré pour Flutter
- ✅ Gestion des erreurs standardisée

## 🛠️ Technologies

- **Backend**: Spring Boot 3.2.x
- **Base de données**: MySQL 8.0+
- **Sécurité**: Spring Security + JWT
- **ORM**: Spring Data JPA + Hibernate
- **Documentation**: OpenAPI 3 (Swagger)
- **Build**: Maven
- **Java**: 17+

## 📋 Prérequis

- Java 17 ou supérieur
- Maven 3.6+
- MySQL 8.0+
- Git

## 🔧 Installation

### 1. Cloner le projet
```bash
git clone <repository-url>
cd agriConnectSpringApi-main
```

### 2. Configuration de la base de données

#### Option A: Docker (Recommandé)
```bash
# Créer un conteneur MySQL
docker run --name agriconnect-mysql \
  -e MYSQL_ROOT_PASSWORD=root_password \
  -e MYSQL_DATABASE=agriconnect_db \
  -e MYSQL_USER=agriconnect_user \
  -e MYSQL_PASSWORD=agriconnect_password \
  -p 3306:3306 \
  -d mysql:8.0 \
  --default-authentication-plugin=mysql_native_password \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

#### Option B: Installation locale
1. Installer MySQL 8.0+
2. Créer une base de données:
```sql
CREATE DATABASE agriconnect_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'agriconnect_user'@'localhost' IDENTIFIED BY 'agriconnect_password';
GRANT ALL PRIVILEGES ON agriconnect_db.* TO 'agriconnect_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configuration de l'application

Modifier `src/main/resources/application.properties` selon votre environnement:

```properties
# Database Configuration (MySQL)
spring.datasource.url=jdbc:mysql://localhost:3306/agriconnect_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8
spring.datasource.username=agriconnect_user
spring.datasource.password=agriconnect_password

# Email Configuration (Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre-email@gmail.com
spring.mail.password=votre-mot-de-passe-app
```

### 4. Initialisation de la base de données

Exécuter le script SQL d'initialisation:
```bash
mysql -h localhost -u agriconnect_user -p agriconnect_db < database/init-mysql.sql
```

### 5. Compilation et lancement

```bash
# Compiler le projet
mvn clean compile

# Lancer l'application
mvn spring-boot:run
```

L'API sera accessible sur: `http://localhost:8080`

## 📚 Documentation API

### Swagger UI
- URL: `http://localhost:8080/swagger-ui.html`
- Documentation interactive de l'API

### Endpoints principaux

#### Authentification
```
POST   /api/v1/auth/register     # Inscription
POST   /api/v1/auth/login        # Connexion
POST   /api/v1/auth/refresh-token # Rafraîchir token
GET    /api/v1/auth/verify       # Vérifier email
POST   /api/v1/auth/forgot-password # Mot de passe oublié
```

#### Produits
```
GET    /api/v1/products          # Liste des produits
GET    /api/v1/products/{id}     # Détail produit
POST   /api/v1/products          # Créer produit (PRODUCTEUR)
PUT    /api/v1/products/{id}     # Modifier produit (PRODUCTEUR)
DELETE /api/v1/products/{id}     # Supprimer produit (PRODUCTEUR)
GET    /api/v1/products/search   # Recherche avec filtres
```

#### Panier
```
GET    /api/v1/cart              # Obtenir panier
POST   /api/v1/cart/items        # Ajouter au panier
PUT    /api/v1/cart/items/{id}   # Modifier quantité
DELETE /api/v1/cart/items/{id}   # Supprimer du panier
POST   /api/v1/cart/promo        # Appliquer code promo
```

#### Commandes
```
GET    /api/v1/orders            # Historique commandes
POST   /api/v1/orders            # Créer commande
GET    /api/v1/orders/{id}       # Détail commande
PUT    /api/v1/orders/{id}/status # Modifier statut
```

## 🔐 Sécurité

### Rôles utilisateurs
- **ACHETEUR**: Peut consulter, acheter, évaluer
- **PRODUCTEUR**: Peut gérer ses produits et commandes
- **ADMIN**: Accès complet (futur)

### Authentification JWT
```bash
# Exemple de requête authentifiée
curl -H "Authorization: Bearer <jwt-token>" \
     http://localhost:8080/api/v1/products
```

## 🧪 Tests

### Tests unitaires
```bash
mvn test
```

### Tests d'intégration
```bash
mvn test -Dtest=*IntegrationTest
```

## 📊 Structure de la base de données

### Tables principales
- `utilisateurs` - Utilisateurs du système
- `acheteurs` - Profils acheteurs
- `producteurs` - Profils producteurs
- `produits` - Catalogue de produits
- `paniers` - Paniers d'achat
- `elements_panier` - Éléments dans les paniers
- `commandes` - Commandes des acheteurs
- `elements_commande` - Éléments des commandes
- `evaluations` - Notes et avis des produits
- `paiements` - Historique des paiements
- `demandes_devis` - Demandes de devis personnalisées

### Relations clés
- Un utilisateur peut être acheteur OU producteur
- Un producteur peut avoir plusieurs produits
- Un acheteur peut avoir un panier et plusieurs commandes
- Chaque commande contient plusieurs éléments
- Les évaluations lient acheteurs, produits et commandes

## 🚀 Déploiement

### Développement avec Docker Compose
```bash
# Démarrer tous les services
docker-compose up -d

# Vérifier les services
docker-compose ps

# Arrêter les services
docker-compose down
```

### Production
```bash
# Configuration automatique
./setup-production.sh

# Démarrage
./start-production.sh

# Arrêt
./stop-production.sh
```

## 📈 Monitoring

### Health Checks
- API: `http://localhost:8080/api/v1/actuator/health`
- MySQL: `docker exec agriconnect-mysql mysqladmin ping`
- Redis: `docker exec agriconnect-redis redis-cli ping`

### Métriques
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

## 🔧 Configuration avancée

### Variables d'environnement
```bash
# Base de données
DATABASE_URL=jdbc:mysql://localhost:3306/agriconnect_db
DATABASE_USERNAME=agriconnect_user
DATABASE_PASSWORD=agriconnect_password

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# Email
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# CORS
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

### Profils Spring Boot
- `dev` - Développement (par défaut)
- `prod` - Production
- `test` - Tests

## 🐛 Dépannage

### Problèmes courants

#### Erreur de connexion MySQL
```bash
# Vérifier que MySQL fonctionne
docker exec agriconnect-mysql mysqladmin ping

# Vérifier les logs
docker logs agriconnect-mysql
```

#### Erreur JWT
```bash
# Vérifier la configuration JWT
echo $JWT_SECRET

# Redémarrer l'application
mvn spring-boot:run
```

#### Problème CORS
```bash
# Vérifier la configuration CORS
curl -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: GET" \
     -X OPTIONS \
     http://localhost:8080/api/v1/products
```

## 📝 Logs

### Niveaux de log
- `DEBUG` - Développement
- `INFO` - Production
- `WARN` - Avertissements
- `ERROR` - Erreurs

### Fichiers de log
- Application: `logs/agriconnect-api.log`
- MySQL: `docker logs agriconnect-mysql`
- Nginx: `docker logs agriconnect-nginx`

## 🤝 Contribution

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

## 📞 Support

- 📧 Email: contact@agriconnect.com
- 📖 Documentation: [Wiki du projet](https://github.com/your-repo/wiki)
- 🐛 Issues: [GitHub Issues](https://github.com/your-repo/issues)

## 🔄 Changelog

### Version 1.0.0 (2024-12-01)
- ✅ Migration de PostgreSQL vers MySQL 8.0
- ✅ Support complet UTF-8 avec utf8mb4
- ✅ Configuration optimisée pour MySQL
- ✅ Scripts de migration et sauvegarde MySQL
- ✅ Tests adaptés pour MySQL
- ✅ Documentation mise à jour

---

**AgriConnect API** - Connecter les producteurs et acheteurs locaux 🚀 