#!/bin/bash

echo "========================================"
echo "Initialisation de la base de données MySQL"
echo "========================================"

echo ""
echo "1. Démarrage du service MySQL..."
sudo systemctl start mysql

echo ""
echo "2. Initialisation de la base de données..."
mysql -u root -p < database/init-mysql.sql

echo ""
echo "3. Vérification de la connexion..."
mysql -u agriconnect_user -pagriconnect_password -e "USE agriconnect_db; SHOW TABLES;"

echo ""
echo "========================================"
echo "Base de données initialisée avec succès!"
echo "========================================"
echo ""
echo "Vous pouvez maintenant démarrer l'application avec: mvn spring-boot:run" 