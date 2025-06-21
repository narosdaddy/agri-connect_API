#!/bin/bash

# Script de déploiement pour AgriConnect API
# Usage: ./deploy.sh [dev|prod]

set -e

ENVIRONMENT=${1:-dev}
APP_NAME="agriConnect-api"
VERSION=$(date +%Y%m%d_%H%M%S)

echo "🚀 Déploiement de AgriConnect API - Environnement: $ENVIRONMENT"
echo "Version: $VERSION"

# Couleurs pour les messages
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction pour afficher les messages
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Vérification des prérequis
check_prerequisites() {
    log_info "Vérification des prérequis..."
    
    # Vérifier Java
    if ! command -v java &> /dev/null; then
        log_error "Java n'est pas installé"
        exit 1
    fi
    
    # Vérifier Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven n'est pas installé"
        exit 1
    fi
    
    # Vérifier Docker (optionnel)
    if command -v docker &> /dev/null; then
        log_info "Docker détecté"
        DOCKER_AVAILABLE=true
    else
        log_warn "Docker non détecté - déploiement local uniquement"
        DOCKER_AVAILABLE=false
    fi
    
    log_info "Prérequis vérifiés ✓"
}

# Configuration selon l'environnement
configure_environment() {
    log_info "Configuration pour l'environnement: $ENVIRONMENT"
    
    case $ENVIRONMENT in
        "dev")
            PROFILE="dev"
            DB_HOST="localhost"
            DB_PORT="5432"
            DB_NAME="agriconnect_db_dev"
            DB_USER="agriconnect_user"
            DB_PASS="agriconnect_password"
            ;;
        "prod")
            PROFILE="prod"
            DB_HOST="${DB_HOST:-localhost}"
            DB_PORT="${DB_PORT:-5432}"
            DB_NAME="${DB_NAME:-agriconnect_db}"
            DB_USER="${DB_USER:-agriconnect_user}"
            DB_PASS="${DB_PASS:-agriconnect_password}"
            ;;
        *)
            log_error "Environnement invalide: $ENVIRONMENT. Utilisez 'dev' ou 'prod'"
            exit 1
            ;;
    esac
    
    log_info "Configuration terminée ✓"
}

# Compilation du projet
build_project() {
    log_info "Compilation du projet..."
    
    # Nettoyer et compiler
    mvn clean compile -q
    
    # Exécuter les tests
    log_info "Exécution des tests..."
    if mvn test -q; then
        log_info "Tests réussis ✓"
    else
        log_error "Tests échoués"
        exit 1
    fi
    
    # Créer le JAR
    log_info "Création du JAR..."
    mvn package -DskipTests -q
    
    log_info "Compilation terminée ✓"
}

# Configuration de la base de données
setup_database() {
    log_info "Configuration de la base de données..."
    
    # Vérifier si PostgreSQL est en cours d'exécution
    if ! pg_isready -h $DB_HOST -p $DB_PORT -U $DB_USER > /dev/null 2>&1; then
        log_error "PostgreSQL n'est pas accessible sur $DB_HOST:$DB_PORT"
        exit 1
    fi
    
    # Créer la base de données si elle n'existe pas
    if ! psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d postgres -c "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'" | grep -q 1; then
        log_info "Création de la base de données: $DB_NAME"
        createdb -h $DB_HOST -p $DB_PORT -U $DB_USER $DB_NAME
    fi
    
    # Exécuter les scripts SQL
    log_info "Exécution des scripts SQL..."
    psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f database/init.sql
    
    log_info "Base de données configurée ✓"
}

# Déploiement avec Docker
deploy_with_docker() {
    if [ "$DOCKER_AVAILABLE" = false ]; then
        log_warn "Docker non disponible, déploiement local"
        return
    fi
    
    log_info "Déploiement avec Docker..."
    
    # Créer l'image Docker
    docker build -t $APP_NAME:$VERSION .
    
    # Arrêter le conteneur existant s'il existe
    docker stop $APP_NAME 2>/dev/null || true
    docker rm $APP_NAME 2>/dev/null || true
    
    # Démarrer le nouveau conteneur
    docker run -d \
        --name $APP_NAME \
        --restart unless-stopped \
        -p 8080:8080 \
        -e SPRING_PROFILES_ACTIVE=$PROFILE \
        -e SPRING_DATASOURCE_URL=jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME \
        -e SPRING_DATASOURCE_USERNAME=$DB_USER \
        -e SPRING_DATASOURCE_PASSWORD=$DB_PASS \
        $APP_NAME:$VERSION
    
    log_info "Déploiement Docker terminé ✓"
}

# Déploiement local
deploy_local() {
    log_info "Déploiement local..."
    
    # Arrêter l'application existante
    pkill -f "agriConnect-api" 2>/dev/null || true
    
    # Démarrer l'application
    nohup java -jar \
        -Dspring.profiles.active=$PROFILE \
        -Dspring.datasource.url=jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME \
        -Dspring.datasource.username=$DB_USER \
        -Dspring.datasource.password=$DB_PASS \
        target/agriConnectSpringApi-main-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
    
    # Attendre que l'application démarre
    sleep 10
    
    # Vérifier que l'application fonctionne
    if curl -f http://localhost:8080/api/v1/actuator/health > /dev/null 2>&1; then
        log_info "Application démarrée avec succès ✓"
    else
        log_error "Échec du démarrage de l'application"
        exit 1
    fi
}

# Vérification post-déploiement
post_deployment_check() {
    log_info "Vérification post-déploiement..."
    
    # Vérifier la santé de l'application
    if curl -f http://localhost:8080/api/v1/actuator/health > /dev/null 2>&1; then
        log_info "Santé de l'application: OK ✓"
    else
        log_error "Problème avec la santé de l'application"
        exit 1
    fi
    
    # Vérifier l'API Swagger
    if curl -f http://localhost:8080/api/v1/swagger-ui.html > /dev/null 2>&1; then
        log_info "Documentation API: OK ✓"
    else
        log_warn "Documentation API non accessible"
    fi
    
    log_info "Vérification post-déploiement terminée ✓"
}

# Affichage des informations de déploiement
show_deployment_info() {
    log_info "=== Informations de déploiement ==="
    echo "Application: $APP_NAME"
    echo "Version: $VERSION"
    echo "Environnement: $ENVIRONMENT"
    echo "URL API: http://localhost:8080/api/v1"
    echo "Documentation: http://localhost:8080/api/v1/swagger-ui.html"
    echo "Santé: http://localhost:8080/api/v1/actuator/health"
    echo "Logs: app.log"
    echo "====================================="
}

# Fonction principale
main() {
    log_info "Début du déploiement..."
    
    check_prerequisites
    configure_environment
    build_project
    setup_database
    
    if [ "$DOCKER_AVAILABLE" = true ] && [ "$ENVIRONMENT" = "prod" ]; then
        deploy_with_docker
    else
        deploy_local
    fi
    
    post_deployment_check
    show_deployment_info
    
    log_info "🎉 Déploiement terminé avec succès!"
}

# Exécution du script
main "$@" 