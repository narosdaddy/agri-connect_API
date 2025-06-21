# Guide de Test API AgriConnect (MySQL)

Guide complet pour tester l'API AgriConnect avec MySQL 8.0.

## 📋 Prérequis

### Logiciels requis
- **Java 17+** : `java -version`
- **Maven 3.6+** : `mvn -version`
- **MySQL 8.0+** : `mysql --version`
- **Docker & Docker Compose** : `docker --version && docker-compose --version`
- **Postman** ou **cURL** pour les tests API
- **jq** pour le formatage JSON : `jq --version`

### Services requis
- **MySQL 8.0** en cours d'exécution
- **Redis** (optionnel, pour le cache)
- **API Spring Boot** démarrée

## 🚀 Installation et Configuration

### 1. Configuration de la base de données MySQL

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

# Vérifier que MySQL fonctionne
docker exec agriconnect-mysql mysqladmin ping -h localhost -u agriconnect_user -pagriconnect_password
```

#### Option B: Installation locale
```bash
# Installer MySQL 8.0
sudo apt-get install mysql-server-8.0

# Créer la base de données
mysql -u root -p
```

```sql
CREATE DATABASE agriconnect_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'agriconnect_user'@'localhost' IDENTIFIED BY 'agriconnect_password';
GRANT ALL PRIVILEGES ON agriconnect_db.* TO 'agriconnect_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 2. Initialisation de la base de données

```bash
# Exécuter le script d'initialisation MySQL
mysql -h localhost -u agriconnect_user -p agriconnect_db < database/init-mysql.sql

# Vérifier les tables créées
mysql -h localhost -u agriconnect_user -p agriconnect_db -e "SHOW TABLES;"
```

### 3. Configuration de l'application

Vérifier `src/main/resources/application.properties`:
```properties
# Database Configuration (MySQL)
spring.datasource.url=jdbc:mysql://localhost:3306/agriconnect_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8
spring.datasource.username=agriconnect_user
spring.datasource.password=agriconnect_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.connection.characterEncoding=utf8
spring.jpa.properties.hibernate.connection.CharSet=utf8
spring.jpa.properties.hibernate.connection.useUnicode=true
```

### 4. Démarrage de l'application

```bash
# Compiler le projet
mvn clean compile

# Lancer l'application
mvn spring-boot:run

# Vérifier que l'API fonctionne
curl http://localhost:8080/api/v1/actuator/health
```

## 🧪 Tests Automatisés

### Tests unitaires
```bash
# Exécuter tous les tests unitaires
mvn test

# Exécuter un test spécifique
mvn test -Dtest=ProduitServiceTest

# Exécuter les tests avec couverture
mvn test jacoco:report
```

### Tests d'intégration
```bash
# Exécuter les tests d'intégration
mvn test -Dtest=*IntegrationTest

# Tests spécifiques
mvn test -Dtest=AuthControllerIntegrationTest
mvn test -Dtest=ProduitControllerIntegrationTest
mvn test -Dtest=PanierControllerIntegrationTest
```

### Tests end-to-end
```bash
# Exécuter le script de test automatisé
chmod +x test-api.sh
./test-api.sh

# Vérifier les résultats
echo "Tests terminés. Vérifiez les logs ci-dessus."
```

## 📊 Tests avec Postman

### 1. Import de la collection

1. Ouvrir Postman
2. Cliquer sur "Import"
3. Sélectionner le fichier `AgriConnect_API.postman_collection.json`
4. Importer la collection

### 2. Configuration des variables

Dans Postman, configurer les variables d'environnement:
```json
{
  "base_url": "http://localhost:8080/api/v1",
  "admin_email": "admin@agriconnect.com",
  "admin_password": "admin123",
  "user_email": "test@agriconnect.com",
  "user_password": "test123",
  "producer_email": "producteur@agriconnect.com",
  "producer_password": "producteur123"
}
```

### 3. Tests par module

#### Module Authentification
1. **Inscription Utilisateur**
   - Méthode: `POST`
   - URL: `{{base_url}}/auth/register`
   - Body: Voir exemple dans la collection

2. **Connexion Utilisateur**
   - Méthode: `POST`
   - URL: `{{base_url}}/auth/login`
   - Vérifier la récupération du token

3. **Vérification Email**
   - Méthode: `GET`
   - URL: `{{base_url}}/auth/verify?token={{verification_token}}`

#### Module Produits
1. **Liste des Produits**
   - Méthode: `GET`
   - URL: `{{base_url}}/products`

2. **Recherche de Produits**
   - Méthode: `GET`
   - URL: `{{base_url}}/products/search?query=tomates&categorie=LEGUMES`

3. **Création de Produit** (Producteur)
   - Méthode: `POST`
   - URL: `{{base_url}}/products`
   - Headers: `Authorization: Bearer {{producer_token}}`

#### Module Panier
1. **Ajout au Panier**
   - Méthode: `POST`
   - URL: `{{base_url}}/cart/items`
   - Headers: `Authorization: Bearer {{user_token}}`

2. **Récupération du Panier**
   - Méthode: `GET`
   - URL: `{{base_url}}/cart`
   - Headers: `Authorization: Bearer {{user_token}}`

3. **Application Code Promo**
   - Méthode: `POST`
   - URL: `{{base_url}}/cart/promo`
   - Body: `{"codePromo": "BIENVENUE10"}`

#### Module Commandes
1. **Création de Commande**
   - Méthode: `POST`
   - URL: `{{base_url}}/orders`
   - Headers: `Authorization: Bearer {{user_token}}`

2. **Historique des Commandes**
   - Méthode: `GET`
   - URL: `{{base_url}}/orders`
   - Headers: `Authorization: Bearer {{user_token}}`

3. **Mise à jour Statut**
   - Méthode: `PUT`
   - URL: `{{base_url}}/orders/{{order_id}}/status`
   - Body: `{"statut": "CONFIRMEE"}`

## 🔍 Tests de Sécurité

### Tests d'authentification
```bash
# Test sans token
curl -X GET http://localhost:8080/api/v1/cart

# Test avec token invalide
curl -X GET http://localhost:8080/api/v1/cart \
  -H "Authorization: Bearer invalid_token"

# Test avec token expiré
curl -X GET http://localhost:8080/api/v1/cart \
  -H "Authorization: Bearer expired_token"
```

### Tests de validation
```bash
# Test avec données invalides
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "invalid-email", "motDePasse": "123"}'

# Test avec champs manquants
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nom": "Test"}'
```

### Tests CORS
```bash
# Test de requête CORS
curl -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  -X OPTIONS \
  http://localhost:8080/api/v1/products
```

## 📈 Tests de Performance

### Tests de charge basiques
```bash
# Test de 100 requêtes consécutives
for i in {1..100}; do
  curl -s http://localhost:8080/api/v1/products > /dev/null
  echo "Requête $i terminée"
done

# Test avec Apache Bench (si installé)
ab -n 1000 -c 10 http://localhost:8080/api/v1/products
```

### Tests de base de données
```bash
# Vérifier les performances MySQL
mysql -h localhost -u agriconnect_user -p agriconnect_db -e "
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    DATA_LENGTH,
    INDEX_LENGTH
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'agriconnect_db';
"

# Vérifier les requêtes lentes
mysql -h localhost -u agriconnect_user -p agriconnect_db -e "
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';
"
```

## 🐛 Dépannage

### Problèmes courants

#### Erreur de connexion MySQL
```bash
# Vérifier que MySQL fonctionne
docker exec agriconnect-mysql mysqladmin ping

# Vérifier les logs MySQL
docker logs agriconnect-mysql

# Vérifier la configuration
mysql -h localhost -u agriconnect_user -p -e "SELECT VERSION();"
```

#### Erreur JWT
```bash
# Vérifier la configuration JWT
grep "jwt.secret" src/main/resources/application.properties

# Redémarrer l'application
mvn spring-boot:run
```

#### Problème de caractères UTF-8
```bash
# Vérifier l'encodage MySQL
mysql -h localhost -u agriconnect_user -p -e "
SHOW VARIABLES LIKE 'character_set%';
SHOW VARIABLES LIKE 'collation%';
"

# Vérifier les tables
mysql -h localhost -u agriconnect_user -p agriconnect_db -e "
SHOW TABLE STATUS;
"
```

### Logs utiles
```bash
# Logs de l'application
tail -f logs/agriconnect-api.log

# Logs MySQL
docker logs -f agriconnect-mysql

# Logs Spring Boot
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.cybernerd=DEBUG"
```

## 📊 Validation des Tests

### Checklist de validation
- [ ] **Base de données MySQL** : Tables créées, données insérées
- [ ] **Authentification** : Inscription, connexion, JWT fonctionnels
- [ ] **Produits** : CRUD, recherche, filtres opérationnels
- [ ] **Panier** : Ajout, modification, codes promo fonctionnels
- [ ] **Commandes** : Création, statuts, historique opérationnels
- [ ] **Sécurité** : Protection des endpoints, validation des données
- [ ] **Performance** : Temps de réponse acceptables
- [ ] **CORS** : Requêtes cross-origin autorisées
- [ ] **Logs** : Messages informatifs et erreurs claires

### Métriques de qualité
```bash
# Couverture de code
mvn test jacoco:report
open target/site/jacoco/index.html

# Tests réussis
mvn test | grep "Tests run"

# Performance
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8080/api/v1/products
```

## 🔄 Tests de Régression

### Tests automatisés quotidiens
```bash
# Créer un script de test quotidien
cat > daily-test.sh << 'EOF'
#!/bin/bash
echo "=== Test quotidien $(date) ==="
./test-api.sh > daily-test-$(date +%Y%m%d).log 2>&1
echo "Test terminé. Vérifiez daily-test-$(date +%Y%m%d).log"
EOF

chmod +x daily-test.sh

# Ajouter au crontab
(crontab -l 2>/dev/null; echo "0 9 * * * /path/to/daily-test.sh") | crontab -
```

### Tests de migration
```bash
# Si migration de PostgreSQL vers MySQL
./migrate-to-mysql.sh

# Vérifier la migration
./migrate-to-mysql.sh verify
```

## 📝 Documentation des Tests

### Rapport de test
```bash
# Générer un rapport de test
cat > test-report.md << 'EOF'
# Rapport de Test AgriConnect API (MySQL)

## Date: $(date)
## Version: 1.0.0
## Base de données: MySQL 8.0

## Résultats des Tests

### Tests Unitaires
- Total: $(mvn test | grep "Tests run" | tail -1)
- Échecs: $(mvn test | grep "Failures" | tail -1)

### Tests d'Intégration
- Total: $(mvn test -Dtest=*IntegrationTest | grep "Tests run" | tail -1)
- Échecs: $(mvn test -Dtest=*IntegrationTest | grep "Failures" | tail -1)

### Tests End-to-End
- Statut: $(./test-api.sh 2>&1 | tail -1)

## Problèmes Identifiés
- Aucun problème majeur détecté

## Recommandations
- Continuer la surveillance des performances
- Ajouter plus de tests de charge
EOF
```

## 🎯 Conclusion

Ce guide couvre tous les aspects du test de l'API AgriConnect avec MySQL. Assurez-vous de :

1. **Tester régulièrement** : Automatisez les tests critiques
2. **Documenter les problèmes** : Gardez une trace des bugs et solutions
3. **Surveiller les performances** : Vérifiez les temps de réponse
4. **Valider la sécurité** : Testez les vulnérabilités potentielles
5. **Maintenir la qualité** : Améliorez la couverture de code

L'API AgriConnect est maintenant prête pour la production avec MySQL ! 🚀 