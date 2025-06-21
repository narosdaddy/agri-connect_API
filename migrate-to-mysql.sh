#!/bin/bash

# Script de migration de PostgreSQL vers MySQL pour AgriConnect
# Usage: ./migrate-to-mysql.sh

set -e

echo "🔄 Migration de PostgreSQL vers MySQL pour AgriConnect API"

# Couleurs pour les messages
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

# Configuration
POSTGRES_CONTAINER="agriconnect-postgres"
MYSQL_CONTAINER="agriconnect-mysql"
BACKUP_DIR="./migration-backup"
MIGRATION_DIR="./migration-scripts"

# Vérification des prérequis
check_prerequisites() {
    log_step "Vérification des prérequis..."
    
    # Vérifier Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker n'est pas installé"
        exit 1
    fi
    
    # Vérifier que PostgreSQL est en cours d'exécution
    if ! docker ps | grep -q $POSTGRES_CONTAINER; then
        log_error "Le conteneur PostgreSQL n'est pas en cours d'exécution"
        exit 1
    fi
    
    # Vérifier que MySQL est en cours d'exécution
    if ! docker ps | grep -q $MYSQL_CONTAINER; then
        log_error "Le conteneur MySQL n'est pas en cours d'exécution"
        exit 1
    fi
    
    log_info "Prérequis vérifiés ✓"
}

# Création des répertoires de sauvegarde
create_backup_directories() {
    log_step "Création des répertoires de sauvegarde..."
    
    mkdir -p $BACKUP_DIR
    mkdir -p $MIGRATION_DIR
    
    log_info "Répertoires créés ✓"
}

# Sauvegarde des données PostgreSQL
backup_postgresql_data() {
    log_step "Sauvegarde des données PostgreSQL..."
    
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_file="$BACKUP_DIR/postgresql_backup_$timestamp.sql"
    
    log_info "Création de la sauvegarde PostgreSQL..."
    docker exec $POSTGRES_CONTAINER pg_dump -U agriconnect_user agriconnect_db > $backup_file
    
    if [ $? -eq 0 ]; then
        log_info "✅ Sauvegarde PostgreSQL créée: $backup_file"
        log_info "Taille: $(du -h $backup_file | cut -f1)"
    else
        log_error "❌ Échec de la sauvegarde PostgreSQL"
        exit 1
    fi
}

# Création du script de migration des données
create_migration_script() {
    log_step "Création du script de migration des données..."
    
    cat > $MIGRATION_DIR/migrate_data.sql << 'EOF'
-- Script de migration des données de PostgreSQL vers MySQL
-- Ce script convertit les données exportées de PostgreSQL vers MySQL

-- Configuration MySQL
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";

-- Désactiver les vérifications de clés étrangères temporairement
SET FOREIGN_KEY_CHECKS = 0;

-- Nettoyer les tables existantes (si nécessaire)
-- TRUNCATE TABLE evaluations;
-- TRUNCATE TABLE elements_commande;
-- TRUNCATE TABLE commandes;
-- TRUNCATE TABLE elements_panier;
-- TRUNCATE TABLE paniers;
-- TRUNCATE TABLE produits;
-- TRUNCATE TABLE producteurs;
-- TRUNCATE TABLE acheteurs;
-- TRUNCATE TABLE administrateurs;
-- TRUNCATE TABLE utilisateurs;

-- Conversion des types de données PostgreSQL vers MySQL
-- Les UUIDs PostgreSQL sont convertis en CHAR(36) MySQL
-- Les timestamps sont convertis en TIMESTAMP MySQL
-- Les booléens sont convertis en BOOLEAN MySQL

-- Exemple de conversion pour la table utilisateurs
-- INSERT INTO utilisateurs (id, nom, email, mot_de_passe, role, telephone, adresse, ville, code_postal, pays, verifie, actif, date_creation, date_modification)
-- SELECT 
--     id::CHAR(36),
--     nom,
--     email,
--     mot_de_passe,
--     role,
--     telephone,
--     adresse,
--     ville,
--     code_postal,
--     COALESCE(pays, 'France'),
--     verifie::BOOLEAN,
--     actif::BOOLEAN,
--     date_creation::TIMESTAMP,
--     date_modification::TIMESTAMP
-- FROM postgresql_utilisateurs;

-- Exemple de conversion pour la table produits
-- INSERT INTO produits (id, producteur_id, nom, description, prix, quantite_disponible, categorie, unite, bio, origine, image_principale, images, disponible, note_moyenne, nombre_avis, nombre_vues, nombre_ventes, date_creation, date_modification)
-- SELECT 
--     id::CHAR(36),
--     producteur_id::CHAR(36),
--     nom,
--     description,
--     prix::DECIMAL(10,2),
--     quantite_disponible::INT,
--     categorie,
--     unite,
--     bio::BOOLEAN,
--     origine,
--     image_principale,
--     images,
--     disponible::BOOLEAN,
--     note_moyenne::DECIMAL(3,2),
--     nombre_avis::INT,
--     nombre_vues::INT,
--     nombre_ventes::INT,
--     date_creation::TIMESTAMP,
--     date_modification::TIMESTAMP
-- FROM postgresql_produits;

-- Réactiver les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 1;

-- Mise à jour des séquences auto-incrémentées (si nécessaire)
-- ALTER TABLE commandes AUTO_INCREMENT = (SELECT MAX(CAST(SUBSTRING(numero_commande, 12) AS UNSIGNED)) + 1 FROM commandes);

COMMIT;

-- Vérification des données migrées
SELECT 'Utilisateurs' as table_name, COUNT(*) as count FROM utilisateurs
UNION ALL
SELECT 'Acheteurs', COUNT(*) FROM acheteurs
UNION ALL
SELECT 'Producteurs', COUNT(*) FROM producteurs
UNION ALL
SELECT 'Produits', COUNT(*) FROM produits
UNION ALL
SELECT 'Paniers', COUNT(*) FROM paniers
UNION ALL
SELECT 'Commandes', COUNT(*) FROM commandes
UNION ALL
SELECT 'Évaluations', COUNT(*) FROM evaluations;
EOF
    
    log_info "✅ Script de migration créé: $MIGRATION_DIR/migrate_data.sql"
}

# Création du script de conversion des types
create_type_conversion_script() {
    log_step "Création du script de conversion des types..."
    
    cat > $MIGRATION_DIR/convert_types.py << 'EOF'
#!/usr/bin/env python3
"""
Script de conversion des types de données PostgreSQL vers MySQL
Usage: python3 convert_types.py postgresql_backup.sql mysql_import.sql
"""

import sys
import re
import uuid
from datetime import datetime

def convert_postgresql_to_mysql(input_file, output_file):
    """Convertit un fichier SQL PostgreSQL en format MySQL"""
    
    with open(input_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Conversions spécifiques PostgreSQL vers MySQL
    conversions = [
        # Types de données
        (r'::uuid', '::CHAR(36)'),
        (r'::boolean', '::BOOLEAN'),
        (r'::integer', '::INT'),
        (r'::bigint', '::BIGINT'),
        (r'::numeric\((\d+),(\d+)\)', r'::DECIMAL(\1,\2)'),
        (r'::text', '::TEXT'),
        (r'::timestamp without time zone', '::TIMESTAMP'),
        (r'::timestamp with time zone', '::TIMESTAMP'),
        
        # Fonctions PostgreSQL vers MySQL
        (r'CURRENT_TIMESTAMP', 'CURRENT_TIMESTAMP'),
        (r'now\(\)', 'CURRENT_TIMESTAMP'),
        (r'uuid_generate_v4\(\)', 'UUID()'),
        
        # Syntaxe spécifique
        (r'DEFAULT nextval\(\'[^\']+\'\)', 'AUTO_INCREMENT'),
        (r'CREATE SEQUENCE [^;]+;', '-- Sequence removed (MySQL uses AUTO_INCREMENT)'),
        (r'ALTER SEQUENCE [^;]+;', '-- Sequence alteration removed'),
        
        # Contraintes
        (r'CONSTRAINT [^)]+\)', ''),
        (r'PRIMARY KEY \([^)]+\)', 'PRIMARY KEY'),
        
        # Index
        (r'CREATE INDEX [^;]+;', '-- Index removed (to be recreated)'),
        (r'CREATE UNIQUE INDEX [^;]+;', '-- Unique index removed (to be recreated)'),
    ]
    
    # Appliquer les conversions
    for pattern, replacement in conversions:
        content = re.sub(pattern, replacement, content, flags=re.IGNORECASE)
    
    # Nettoyer les lignes vides multiples
    content = re.sub(r'\n\s*\n\s*\n', '\n\n', content)
    
    # Ajouter l'en-tête MySQL
    mysql_header = """-- Script converti de PostgreSQL vers MySQL
-- Généré automatiquement le """ + datetime.now().strftime('%Y-%m-%d %H:%M:%S') + """
-- ATTENTION: Vérifiez et ajustez ce script avant exécution

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";

"""
    
    mysql_footer = """

-- Réactivation des contraintes
SET FOREIGN_KEY_CHECKS = 1;

COMMIT;
"""
    
    content = mysql_header + content + mysql_footer
    
    # Écrire le fichier converti
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print(f"✅ Conversion terminée: {output_file}")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python3 convert_types.py postgresql_backup.sql mysql_import.sql")
        sys.exit(1)
    
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    
    try:
        convert_postgresql_to_mysql(input_file, output_file)
    except Exception as e:
        print(f"❌ Erreur lors de la conversion: {e}")
        sys.exit(1)
EOF
    
    chmod +x $MIGRATION_DIR/convert_types.py
    log_info "✅ Script de conversion des types créé: $MIGRATION_DIR/convert_types.py"
}

# Création du script de vérification
create_verification_script() {
    log_step "Création du script de vérification..."
    
    cat > $MIGRATION_DIR/verify_migration.sql << 'EOF'
-- Script de vérification de la migration PostgreSQL vers MySQL

-- Vérification de la structure des tables
SHOW TABLES;

-- Vérification des données
SELECT '=== VÉRIFICATION DES DONNÉES ===' as info;

-- Comptage des enregistrements
SELECT 'Utilisateurs' as table_name, COUNT(*) as count FROM utilisateurs
UNION ALL
SELECT 'Acheteurs', COUNT(*) FROM acheteurs
UNION ALL
SELECT 'Producteurs', COUNT(*) FROM producteurs
UNION ALL
SELECT 'Produits', COUNT(*) FROM produits
UNION ALL
SELECT 'Paniers', COUNT(*) FROM paniers
UNION ALL
SELECT 'Éléments Panier', COUNT(*) FROM elements_panier
UNION ALL
SELECT 'Commandes', COUNT(*) FROM commandes
UNION ALL
SELECT 'Éléments Commande', COUNT(*) FROM elements_commande
UNION ALL
SELECT 'Évaluations', COUNT(*) FROM evaluations
UNION ALL
SELECT 'Paiements', COUNT(*) FROM paiements;

-- Vérification des contraintes
SELECT '=== VÉRIFICATION DES CONTRAINTES ===' as info;
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE
FROM information_schema.TABLE_CONSTRAINTS 
WHERE TABLE_SCHEMA = 'agriconnect_db'
ORDER BY TABLE_NAME, CONSTRAINT_TYPE;

-- Vérification des index
SELECT '=== VÉRIFICATION DES INDEX ===' as info;
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = 'agriconnect_db'
ORDER BY TABLE_NAME, INDEX_NAME;

-- Vérification des types de données
SELECT '=== VÉRIFICATION DES TYPES ===' as info;
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'agriconnect_db'
ORDER BY TABLE_NAME, ORDINAL_POSITION;

-- Vérification des relations
SELECT '=== VÉRIFICATION DES RELATIONS ===' as info;
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'agriconnect_db' 
    AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME, COLUMN_NAME;

-- Test de quelques requêtes
SELECT '=== TESTS DE REQUÊTES ===' as info;

-- Test 1: Produits avec leurs producteurs
SELECT 
    p.nom as produit,
    pr.nom_exploitation as exploitation,
    p.prix,
    p.categorie
FROM produits p
JOIN producteurs pr ON p.producteur_id = pr.id
LIMIT 5;

-- Test 2: Commandes avec leurs éléments
SELECT 
    c.numero_commande,
    c.statut,
    c.total,
    COUNT(ec.id) as nombre_elements
FROM commandes c
LEFT JOIN elements_commande ec ON c.id = ec.commande_id
GROUP BY c.id
LIMIT 5;

-- Test 3: Évaluations moyennes par produit
SELECT 
    p.nom as produit,
    AVG(e.note) as note_moyenne,
    COUNT(e.id) as nombre_avis
FROM produits p
LEFT JOIN evaluations e ON p.id = e.produit_id
GROUP BY p.id
HAVING nombre_avis > 0
ORDER BY note_moyenne DESC
LIMIT 5;

SELECT '=== MIGRATION TERMINÉE ===' as info;
EOF
    
    log_info "✅ Script de vérification créé: $MIGRATION_DIR/verify_migration.sql"
}

# Création du script de rollback
create_rollback_script() {
    log_step "Création du script de rollback..."
    
    cat > $MIGRATION_DIR/rollback.sh << 'EOF'
#!/bin/bash

# Script de rollback pour revenir à PostgreSQL
# Usage: ./rollback.sh

set -e

echo "🔄 Rollback vers PostgreSQL"

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Arrêter MySQL
log_info "Arrêt de MySQL..."
docker stop agriconnect-mysql || true

# Redémarrer PostgreSQL
log_info "Redémarrage de PostgreSQL..."
docker start agriconnect-postgres || true

# Restaurer la configuration PostgreSQL
log_info "Restauration de la configuration PostgreSQL..."

# Restaurer le pom.xml
git checkout HEAD -- pom.xml

# Restaurer application.properties
git checkout HEAD -- src/main/resources/application.properties

# Restaurer docker-compose.yml
git checkout HEAD -- docker-compose.yml

log_info "✅ Rollback terminé"
log_warn "⚠️  Redémarrez l'application pour utiliser PostgreSQL"
EOF
    
    chmod +x $MIGRATION_DIR/rollback.sh
    log_info "✅ Script de rollback créé: $MIGRATION_DIR/rollback.sh"
}

# Exécution de la migration
execute_migration() {
    log_step "Exécution de la migration..."
    
    # Trouver le fichier de sauvegarde le plus récent
    local latest_backup=$(ls -t $BACKUP_DIR/postgresql_backup_*.sql 2>/dev/null | head -1)
    
    if [ -z "$latest_backup" ]; then
        log_error "❌ Aucun fichier de sauvegarde trouvé"
        exit 1
    fi
    
    log_info "Utilisation de la sauvegarde: $latest_backup"
    
    # Convertir le fichier
    local converted_file="$MIGRATION_DIR/converted_data.sql"
    python3 $MIGRATION_DIR/convert_types.py "$latest_backup" "$converted_file"
    
    if [ $? -eq 0 ]; then
        log_info "✅ Conversion terminée"
        
        # Importer dans MySQL
        log_info "Import des données dans MySQL..."
        docker exec -i $MYSQL_CONTAINER mysql -u agriconnect_user -pagriconnect_password agriconnect_db < "$converted_file"
        
        if [ $? -eq 0 ]; then
            log_info "✅ Import MySQL réussi"
        else
            log_error "❌ Échec de l'import MySQL"
            exit 1
        fi
    else
        log_error "❌ Échec de la conversion"
        exit 1
    fi
}

# Vérification de la migration
verify_migration() {
    log_step "Vérification de la migration..."
    
    # Exécuter le script de vérification
    docker exec -i $MYSQL_CONTAINER mysql -u agriconnect_user -pagriconnect_password agriconnect_db < $MIGRATION_DIR/verify_migration.sql
    
    log_info "✅ Vérification terminée"
}

# Affichage des instructions finales
show_final_instructions() {
    log_info "=== Migration PostgreSQL vers MySQL terminée ==="
    echo ""
    echo "📁 Fichiers créés:"
    echo "   - $BACKUP_DIR/ (sauvegardes PostgreSQL)"
    echo "   - $MIGRATION_DIR/ (scripts de migration)"
    echo ""
    echo "📋 Prochaines étapes:"
    echo "1. Vérifiez les données migrées:"
    echo "   docker exec -i $MYSQL_CONTAINER mysql -u agriconnect_user -pagriconnect_password agriconnect_db < $MIGRATION_DIR/verify_migration.sql"
    echo ""
    echo "2. Testez l'application avec MySQL:"
    echo "   ./test-api.sh"
    echo ""
    echo "3. Si nécessaire, revenez à PostgreSQL:"
    echo "   ./$MIGRATION_DIR/rollback.sh"
    echo ""
    echo "🔧 Configuration mise à jour:"
    echo "   - pom.xml: Dépendance MySQL"
    echo "   - application.properties: URL MySQL"
    echo "   - docker-compose.yml: Conteneur MySQL"
    echo ""
    echo "🎉 Migration terminée avec succès!"
}

# Fonction principale
main() {
    log_info "Début de la migration PostgreSQL vers MySQL..."
    
    check_prerequisites
    create_backup_directories
    backup_postgresql_data
    create_migration_script
    create_type_conversion_script
    create_verification_script
    create_rollback_script
    execute_migration
    verify_migration
    show_final_instructions
    
    log_info "🎉 Migration terminée!"
}

# Exécution du script
main "$@" 