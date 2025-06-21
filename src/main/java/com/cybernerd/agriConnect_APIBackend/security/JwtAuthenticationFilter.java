package com.cybernerd.agriConnect_APIBackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Ignorer l'authentification JWT pour les endpoints publics
        String path = request.getRequestURI();
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        
        // Vérifier si l'en-tête Authorization existe et commence par "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extraire le token JWT (enlever "Bearer ")
        jwt = authHeader.substring(7);
        
        try {
            // Extraire l'email du token
            userEmail = jwtService.extractUsername(jwt);
            
            // Si l'email est extrait et qu'aucune authentification n'est déjà établie
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Charger les détails de l'utilisateur
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                
                // Vérifier si le token est valide
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    
                    // Créer un token d'authentification
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    // Ajouter les détails de la requête
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // Définir l'authentification dans le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.debug("Utilisateur authentifié: {}", userEmail);
                } else {
                    log.warn("Token JWT invalide pour l'utilisateur: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'authentification JWT", e);
            // Ne pas bloquer la requête en cas d'erreur, laisser passer
        }
        
        // Continuer avec le filtre suivant
        filterChain.doFilter(request, response);
    }
    
    /**
     * Vérifie si l'endpoint est public (ne nécessite pas d'authentification)
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/v1/auth/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/api-docs/") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/error") ||
               path.startsWith("/uploads/") ||
               (path.startsWith("/api/v1/produits") && path.length() > "/api/v1/produits".length()) ||
               path.equals("/api/v1/produits") ||
               path.equals("/api/v1/produits/search") ||
               path.startsWith("/api/v1/produits/categorie/") ||
               path.startsWith("/api/v1/produits/producteur/");
    }
}

