-- Script d'initialisation de la base de données MySQL pour AgriConnect
-- À exécuter en tant qu'utilisateur root MySQL

-- Créer la base de données
CREATE DATABASE IF NOT EXISTS agriconnect_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Créer l'utilisateur avec les privilèges
CREATE USER IF NOT EXISTS 'agriconnect_user'@'localhost' IDENTIFIED BY 'agriconnect_password';
CREATE USER IF NOT EXISTS 'agriconnect_user'@'%' IDENTIFIED BY 'agriconnect_password';

-- Accorder les privilèges
GRANT ALL PRIVILEGES ON agriconnect_db.* TO 'agriconnect_user'@'localhost';
GRANT ALL PRIVILEGES ON agriconnect_db.* TO 'agriconnect_user'@'%';

-- Appliquer les changements
FLUSH PRIVILEGES;

-- Utiliser la base de données
USE agriconnect_db;

-- Créer les tables de base (optionnel, Hibernate peut les créer automatiquement)
-- Les tables seront créées automatiquement par Hibernate avec spring.jpa.hibernate.ddl-auto=create-drop

-- Afficher un message de confirmation
SELECT 'Base de données AgriConnect initialisée avec succès!' AS message; 