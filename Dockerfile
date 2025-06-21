# Dockerfile pour AgriConnect API
FROM openjdk:17-jdk-slim

# Métadonnées
LABEL maintainer="AgriConnect Team"
LABEL version="1.0"
LABEL description="AgriConnect Spring Boot API"

# Variables d'environnement
ENV APP_NAME=agriConnect-api
ENV APP_VERSION=1.0.0
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# Créer l'utilisateur non-root
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Créer le répertoire de travail
WORKDIR /app

# Copier le fichier JAR
COPY target/agriConnectSpringApi-main-0.0.1-SNAPSHOT.jar app.jar

# Créer le répertoire pour les uploads
RUN mkdir -p /app/uploads/produits && \
    chown -R appuser:appuser /app

# Changer vers l'utilisateur non-root
USER appuser

# Exposer le port
EXPOSE 8080

# Point d'entrée
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Commande par défaut
CMD ["--spring.profiles.active=prod"] 