@echo off
echo Testing API endpoints...
echo.

echo 1. Testing health endpoint:
curl -s http://localhost:8080/api/v1/actuator/health
echo.
echo.

echo 2. Testing auth register endpoint (GET):
curl -s http://localhost:8080/api/v1/auth/register
echo.
echo.

echo 3. Testing auth register endpoint (POST):
curl -s -X POST http://localhost:8080/api/v1/auth/register -H "Content-Type: application/json" -d "{\"nom\":\"Test User\",\"email\":\"test@example.com\",\"motDePasse\":\"password123\",\"role\":\"ACHETEUR\",\"telephone\":\"123456789\"}"
echo.
echo.

echo 4. Testing products endpoint:
curl -s http://localhost:8080/api/v1/produits
echo.
echo.

echo API testing completed.
pause 