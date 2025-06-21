#!/bin/bash

# Script de configuration de production pour AgriConnect API avec MySQL
# Usage: ./setup-production.sh

set -e

echo "🔧 Configuration de l'environnement de production AgriConnect API (MySQL)"

# Couleurs pour les messages
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Vérification des prérequis
check_prerequisites() {
    log_step "Vérification des prérequis..."
    
    # Vérifier Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker n'est pas installé"
        exit 1
    fi
    
    # Vérifier Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose n'est pas installé"
        exit 1
    fi
    
    # Vérifier Git
    if ! command -v git &> /dev/null; then
        log_error "Git n'est pas installé"
        exit 1
    fi
    
    log_info "Prérequis vérifiés ✓"
}

# Configuration des variables d'environnement
setup_environment_variables() {
    log_step "Configuration des variables d'environnement..."
    
    # Créer le fichier .env s'il n'existe pas
    if [ ! -f .env ]; then
        log_info "Création du fichier .env..."
        cat > .env << EOF
# Configuration de la base de données MySQL
DATABASE_URL=jdbc:mysql://localhost:3306/agriconnect_db_prod?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8
DATABASE_USERNAME=agriconnect_user
DATABASE_PASSWORD=agriconnect_secure_password_2024

# Configuration JWT
JWT_SECRET=agriConnectProductionSecretKey2024VeryLongAndSecureForProductionUseWithSpecialCharacters123!@#$%^&*()

# Configuration des emails
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=contact@agriconnect.com
MAIL_PASSWORD=your_secure_app_password

# Configuration Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Configuration CORS
CORS_ALLOWED_ORIGINS=https://agriconnect.com,https://www.agriconnect.com,https://app.agriconnect.com

# Configuration Swagger
SWAGGER_ENABLED=false

# Configuration Admin
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin_secure_password_2024

# Configuration Stripe
STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key
STRIPE_PUBLISHABLE_KEY=pk_test_your_stripe_publishable_key
STRIPE_WEBHOOK_SECRET=whsec_your_stripe_webhook_secret

# Configuration Firebase
FIREBASE_SERVER_KEY=your_firebase_server_key

# Configuration Google Maps
GOOGLE_MAPS_API_KEY=your_google_maps_api_key

# Configuration Analytics
ANALYTICS_TRACKING_ID=your_analytics_tracking_id

# Configuration IP Whitelist
IP_WHITELIST=127.0.0.1,::1

# Configuration Blockchain
BLOCKCHAIN_CONTRACT_ADDRESS=0x0000000000000000000000000000000000000000
EOF
        log_info "Fichier .env créé ✓"
    else
        log_warn "Le fichier .env existe déjà"
    fi
    
    log_info "Variables d'environnement configurées ✓"
}

# Configuration de la base de données MySQL
setup_database() {
    log_step "Configuration de la base de données MySQL..."
    
    # Créer le conteneur MySQL
    log_info "Démarrage de MySQL..."
    docker run -d \
        --name agriconnect-mysql-prod \
        --restart unless-stopped \
        -e MYSQL_ROOT_PASSWORD=root_secure_password_2024 \
        -e MYSQL_DATABASE=agriconnect_db_prod \
        -e MYSQL_USER=agriconnect_user \
        -e MYSQL_PASSWORD=agriconnect_secure_password_2024 \
        -p 3306:3306 \
        -v mysql_data_prod:/var/lib/mysql \
        mysql:8.0 \
        --default-authentication-plugin=mysql_native_password \
        --character-set-server=utf8mb4 \
        --collation-server=utf8mb4_unicode_ci
    
    # Attendre que MySQL démarre
    log_info "Attente du démarrage de MySQL..."
    sleep 30
    
    # Vérifier que MySQL fonctionne
    if docker exec agriconnect-mysql-prod mysqladmin ping -h localhost -u agriconnect_user -pagriconnect_secure_password_2024; then
        log_info "MySQL démarré avec succès ✓"
    else
        log_error "Échec du démarrage de MySQL"
        exit 1
    fi
    
    # Exécuter les scripts SQL
    log_info "Exécution des scripts SQL..."
    docker exec -i agriconnect-mysql-prod mysql -u agriconnect_user -pagriconnect_secure_password_2024 agriconnect_db_prod < database/init-mysql.sql
    
    log_info "Base de données configurée ✓"
}

# Configuration de Redis
setup_redis() {
    log_step "Configuration de Redis..."
    
    # Créer le conteneur Redis
    log_info "Démarrage de Redis..."
    docker run -d \
        --name agriconnect-redis-prod \
        --restart unless-stopped \
        -p 6379:6379 \
        -v redis_data_prod:/data \
        redis:7-alpine redis-server --appendonly yes
    
    # Attendre que Redis démarre
    log_info "Attente du démarrage de Redis..."
    sleep 5
    
    # Vérifier que Redis fonctionne
    if docker exec agriconnect-redis-prod redis-cli ping | grep -q PONG; then
        log_info "Redis démarré avec succès ✓"
    else
        log_error "Échec du démarrage de Redis"
        exit 1
    fi
    
    log_info "Redis configuré ✓"
}

# Configuration de Nginx
setup_nginx() {
    log_step "Configuration de Nginx..."
    
    # Créer la configuration Nginx
    log_info "Création de la configuration Nginx..."
    cat > nginx.conf << EOF
events {
    worker_connections 1024;
}

http {
    upstream api_backend {
        server api:8080;
    }
    
    server {
        listen 80;
        server_name agriconnect.com www.agriconnect.com;
        
        # Redirection HTTPS
        return 301 https://\$server_name\$request_uri;
    }
    
    server {
        listen 443 ssl http2;
        server_name agriconnect.com www.agriconnect.com;
        
        # SSL Configuration
        ssl_certificate /etc/nginx/ssl/agriconnect.crt;
        ssl_certificate_key /etc/nginx/ssl/agriconnect.key;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
        ssl_prefer_server_ciphers off;
        ssl_session_cache shared:SSL:10m;
        ssl_session_timeout 10m;
        
        # Security Headers
        add_header X-Frame-Options DENY;
        add_header X-Content-Type-Options nosniff;
        add_header X-XSS-Protection "1; mode=block";
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
        
        # API Proxy
        location /api/ {
            proxy_pass http://api_backend;
            proxy_set_header Host \$host;
            proxy_set_header X-Real-IP \$remote_addr;
            proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto \$scheme;
            proxy_connect_timeout 30s;
            proxy_send_timeout 30s;
            proxy_read_timeout 30s;
        }
        
        # Static Files
        location /uploads/ {
            alias /usr/share/nginx/html/uploads/;
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
        
        # Health Check
        location /health {
            proxy_pass http://api_backend/api/v1/actuator/health;
            access_log off;
        }
        
        # Root location
        location / {
            return 404;
        }
    }
}
EOF
    
    log_info "Configuration Nginx créée ✓"
}

# Configuration des certificats SSL
setup_ssl() {
    log_step "Configuration des certificats SSL..."
    
    # Créer le répertoire pour les certificats
    mkdir -p ssl
    
    log_warn "⚠️  IMPORTANT: Vous devez placer vos certificats SSL dans le dossier ssl/"
    log_warn "   - agriconnect.crt (certificat public)"
    log_warn "   - agriconnect.key (clé privée)"
    log_warn "   Vous pouvez utiliser Let's Encrypt ou un autre fournisseur de certificats"
    
    # Créer des certificats auto-signés pour le développement
    if [ ! -f ssl/agriconnect.crt ] || [ ! -f ssl/agriconnect.key ]; then
        log_info "Génération de certificats auto-signés pour le développement..."
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout ssl/agriconnect.key \
            -out ssl/agriconnect.crt \
            -subj "/C=FR/ST=France/L=Paris/O=AgriConnect/CN=agriconnect.com"
        log_warn "Certificats auto-signés générés (à remplacer en production)"
    fi
    
    log_info "Certificats SSL configurés ✓"
}

# Configuration des logs
setup_logging() {
    log_step "Configuration des logs..."
    
    # Créer le répertoire pour les logs
    mkdir -p logs
    
    # Créer la configuration logrotate
    log_info "Configuration de logrotate..."
    cat > /etc/logrotate.d/agriconnect << EOF
/var/log/agriconnect/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 agriconnect agriconnect
    postrotate
        systemctl reload nginx
    endscript
}
EOF
    
    log_info "Configuration des logs terminée ✓"
}

# Configuration du monitoring
setup_monitoring() {
    log_step "Configuration du monitoring..."
    
    # Créer la configuration Prometheus
    log_info "Configuration de Prometheus..."
    cat > prometheus.yml << EOF
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"

scrape_configs:
  - job_name: 'agriconnect-api'
    static_configs:
      - targets: ['api:8080']
    metrics_path: '/api/v1/actuator/prometheus'
    scrape_interval: 30s

  - job_name: 'mysql'
    static_configs:
      - targets: ['mysql:3306']

  - job_name: 'redis'
    static_configs:
      - targets: ['redis:6379']
EOF
    
    # Créer la configuration Grafana
    log_info "Configuration de Grafana..."
    mkdir -p grafana/provisioning/datasources
    cat > grafana/provisioning/datasources/datasource.yml << EOF
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
EOF
    
    log_info "Configuration du monitoring terminée ✓"
}

# Configuration de la sauvegarde
setup_backup() {
    log_step "Configuration de la sauvegarde..."
    
    # Créer le script de sauvegarde
    log_info "Création du script de sauvegarde..."
    cat > backup.sh << 'EOF'
#!/bin/bash

# Script de sauvegarde pour AgriConnect avec MySQL
BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=30

# Créer le répertoire de sauvegarde
mkdir -p $BACKUP_DIR

# Sauvegarde de la base de données MySQL
echo "Sauvegarde de la base de données MySQL..."
docker exec agriconnect-mysql-prod mysqldump -u agriconnect_user -pagriconnect_secure_password_2024 agriconnect_db_prod > $BACKUP_DIR/db_backup_$DATE.sql

# Sauvegarde des fichiers uploadés
echo "Sauvegarde des fichiers uploadés..."
tar -czf $BACKUP_DIR/uploads_backup_$DATE.tar.gz -C /app uploads/

# Sauvegarde des logs
echo "Sauvegarde des logs..."
tar -czf $BACKUP_DIR/logs_backup_$DATE.tar.gz -C /app logs/

# Compression de la sauvegarde complète
echo "Compression de la sauvegarde complète..."
tar -czf $BACKUP_DIR/agriconnect_backup_$DATE.tar.gz \
    $BACKUP_DIR/db_backup_$DATE.sql \
    $BACKUP_DIR/uploads_backup_$DATE.tar.gz \
    $BACKUP_DIR/logs_backup_$DATE.tar.gz

# Nettoyage des fichiers temporaires
rm $BACKUP_DIR/db_backup_$DATE.sql
rm $BACKUP_DIR/uploads_backup_$DATE.tar.gz
rm $BACKUP_DIR/logs_backup_$DATE.tar.gz

# Suppression des anciennes sauvegardes
find $BACKUP_DIR -name "agriconnect_backup_*.tar.gz" -mtime +$RETENTION_DAYS -delete

echo "Sauvegarde terminée: $BACKUP_DIR/agriconnect_backup_$DATE.tar.gz"
EOF
    
    chmod +x backup.sh
    
    # Ajouter au crontab
    log_info "Configuration du crontab pour la sauvegarde automatique..."
    (crontab -l 2>/dev/null; echo "0 2 * * * /app/backup.sh") | crontab -
    
    log_info "Configuration de la sauvegarde terminée ✓"
}

# Configuration de la sécurité
setup_security() {
    log_step "Configuration de la sécurité..."
    
    # Configuration du firewall
    log_info "Configuration du firewall..."
    ufw --force enable
    ufw default deny incoming
    ufw default allow outgoing
    ufw allow ssh
    ufw allow 80/tcp
    ufw allow 443/tcp
    ufw allow 22/tcp
    
    # Configuration des utilisateurs
    log_info "Configuration des utilisateurs..."
    useradd -m -s /bin/bash agriconnect
    usermod -aG docker agriconnect
    
    # Configuration des permissions
    log_info "Configuration des permissions..."
    chown -R agriconnect:agriconnect /app
    chmod -R 755 /app
    chmod 600 /app/.env
    
    log_info "Configuration de la sécurité terminée ✓"
}

# Configuration finale
finalize_setup() {
    log_step "Finalisation de la configuration..."
    
    # Créer le script de démarrage
    log_info "Création du script de démarrage..."
    cat > start-production.sh << 'EOF'
#!/bin/bash

# Script de démarrage de production avec MySQL
echo "🚀 Démarrage d'AgriConnect API en production (MySQL)..."

# Charger les variables d'environnement
set -a
source .env
set +a

# Démarrer les services
docker-compose -f docker-compose.prod.yml up -d

# Vérifier le statut des services
echo "Vérification du statut des services..."
sleep 10

if curl -f https://localhost/health > /dev/null 2>&1; then
    echo "✅ Services démarrés avec succès"
    echo "🌐 API disponible sur: https://agriconnect.com/api/v1"
    echo "📊 Monitoring disponible sur: http://localhost:3000"
    echo "🗄️  MySQL disponible sur: localhost:3306"
else
    echo "❌ Erreur lors du démarrage des services"
    exit 1
fi
EOF
    
    chmod +x start-production.sh
    
    # Créer le script d'arrêt
    log_info "Création du script d'arrêt..."
    cat > stop-production.sh << 'EOF'
#!/bin/bash

# Script d'arrêt de production
echo "🛑 Arrêt d'AgriConnect API..."

# Arrêter les services
docker-compose -f docker-compose.prod.yml down

echo "✅ Services arrêtés"
EOF
    
    chmod +x stop-production.sh
    
    log_info "Configuration finale terminée ✓"
}

# Affichage des informations finales
show_final_info() {
    log_info "=== Configuration de production terminée (MySQL) ==="
    echo ""
    echo "📋 Prochaines étapes:"
    echo "1. Modifier le fichier .env avec vos vraies valeurs"
    echo "2. Remplacer les certificats SSL dans ssl/"
    echo "3. Configurer votre domaine DNS"
    echo "4. Démarrer les services: ./start-production.sh"
    echo ""
    echo "🔗 URLs importantes:"
    echo "   - API: https://agriconnect.com/api/v1"
    echo "   - Documentation: https://agriconnect.com/api/v1/swagger-ui.html"
    echo "   - Monitoring: http://localhost:3000"
    echo "   - Santé: https://agriconnect.com/health"
    echo "   - MySQL: localhost:3306"
    echo ""
    echo "📁 Fichiers créés:"
    echo "   - .env (variables d'environnement)"
    echo "   - nginx.conf (configuration Nginx)"
    echo "   - prometheus.yml (configuration monitoring)"
    echo "   - backup.sh (script de sauvegarde MySQL)"
    echo "   - start-production.sh (script de démarrage)"
    echo "   - stop-production.sh (script d'arrêt)"
    echo ""
    echo "🔒 Sécurité:"
    echo "   - Firewall configuré"
    echo "   - Utilisateur agriconnect créé"
    echo "   - Permissions sécurisées"
    echo ""
    echo "📊 Monitoring:"
    echo "   - Prometheus configuré"
    echo "   - Grafana configuré"
    echo "   - Logs centralisés"
    echo ""
    echo "💾 Sauvegarde:"
    echo "   - Sauvegarde automatique quotidienne MySQL"
    echo "   - Rétention de 30 jours"
    echo ""
    echo "🗄️  Base de données MySQL:"
    echo "   - Port: 3306"
    echo "   - Base: agriconnect_db_prod"
    echo "   - Utilisateur: agriconnect_user"
    echo "   - Script d'initialisation: database/init-mysql.sql"
    echo ""
    echo "🎉 Configuration terminée avec succès!"
}

# Fonction principale
main() {
    log_info "Début de la configuration de production avec MySQL..."
    
    check_prerequisites
    setup_environment_variables
    setup_database
    setup_redis
    setup_nginx
    setup_ssl
    setup_logging
    setup_monitoring
    setup_backup
    setup_security
    finalize_setup
    show_final_info
    
    log_info "🎉 Configuration de production terminée!"
}

# Exécution du script
main "$@" 