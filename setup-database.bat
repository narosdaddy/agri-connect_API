@echo off
echo ========================================
echo Initialisation de la base de donnees MySQL
echo ========================================

echo.
echo 1. Demarrage du service MySQL...
net start mysql

echo.
echo 2. Initialisation de la base de donnees...
mysql -u root -p < database/init-mysql.sql

echo.
echo 3. Verification de la connexion...
mysql -u agriconnect_user -pagriconnect_password -e "USE agriconnect_db; SHOW TABLES;"

echo.
echo ========================================
echo Base de donnees initialisee avec succes!
echo ========================================
echo.
echo Vous pouvez maintenant demarrer l'application avec: mvn spring-boot:run
pause 