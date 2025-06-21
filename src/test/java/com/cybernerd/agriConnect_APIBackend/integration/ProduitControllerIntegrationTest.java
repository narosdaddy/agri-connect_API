package com.cybernerd.agriConnect_APIBackend.integration;

import com.cybernerd.agriConnect_APIBackend.dtos.auth.AuthResponse;
import com.cybernerd.agriConnect_APIBackend.dtos.auth.LoginRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.auth.RegisterRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.produit.ProduitRequest;
import com.cybernerd.agriConnect_APIBackend.enumType.CategorieProduit;
import com.cybernerd.agriConnect_APIBackend.enumType.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProduitControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String producteurToken;
    private UUID producteurId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Créer un producteur et obtenir son token
        setupProducteur();
    }

    private void setupProducteur() throws Exception {
        // Enregistrer un producteur
        RegisterRequest registerRequest = RegisterRequest.builder()
                .nom("Test Producteur")
                .email("producteur@example.com")
                .motDePasse("password123")
                .role(Role.PRODUCTEUR)
                .nomExploitation("Ferme Test")
                .certifieBio(true)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Se connecter pour obtenir le token
        LoginRequest loginRequest = LoginRequest.builder()
                .email("producteur@example.com")
                .motDePasse("password123")
                .build();

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);
        producteurToken = authResponse.getToken();
        producteurId = authResponse.getId();
    }

    @Test
    void createProduit_Success() throws Exception {
        ProduitRequest request = ProduitRequest.builder()
                .nom("Tomates Bio")
                .description("Tomates biologiques fraîches")
                .prix(new BigDecimal("4.50"))
                .quantiteDisponible(100)
                .categorie(CategorieProduit.LEGUMES)
                .unite("kg")
                .bio(true)
                .origine("Lyon")
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + producteurToken)
                        .param("producteurId", producteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Tomates Bio"))
                .andExpect(jsonPath("$.prix").value(4.50))
                .andExpect(jsonPath("$.categorie").value("LEGUMES"))
                .andExpect(jsonPath("$.bio").value(true))
                .andExpect(jsonPath("$.producteurId").value(producteurId.toString()));
    }

    @Test
    void createProduit_Unauthorized_ShouldReturnError() throws Exception {
        ProduitRequest request = ProduitRequest.builder()
                .nom("Tomates Bio")
                .description("Tomates biologiques fraîches")
                .prix(new BigDecimal("4.50"))
                .quantiteDisponible(100)
                .categorie(CategorieProduit.LEGUMES)
                .unite("kg")
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .param("producteurId", producteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createProduit_InvalidData_ShouldReturnError() throws Exception {
        ProduitRequest request = ProduitRequest.builder()
                .nom("") // Nom vide
                .prix(new BigDecimal("-1")) // Prix négatif
                .quantiteDisponible(-10) // Quantité négative
                .categorie(CategorieProduit.LEGUMES)
                .unite("kg")
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + producteurToken)
                        .param("producteurId", producteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllProduits_Success() throws Exception {
        // Créer un produit d'abord
        ProduitRequest request = ProduitRequest.builder()
                .nom("Pommes Golden")
                .description("Pommes Golden Delicious")
                .prix(new BigDecimal("3.20"))
                .quantiteDisponible(50)
                .categorie(CategorieProduit.FRUITS)
                .unite("kg")
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + producteurToken)
                        .param("producteurId", producteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Récupérer tous les produits
        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].nom").value("Pommes Golden"));
    }

    @Test
    void getProduitById_Success() throws Exception {
        // Créer un produit
        ProduitRequest request = ProduitRequest.builder()
                .nom("Carottes Bio")
                .description("Carottes biologiques")
                .prix(new BigDecimal("2.80"))
                .quantiteDisponible(75)
                .categorie(CategorieProduit.LEGUMES)
                .unite("kg")
                .bio(true)
                .build();

        String response = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + producteurToken)
                        .param("producteurId", producteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extraire l'ID du produit créé
        String produitId = objectMapper.readTree(response).get("id").asText();

        // Récupérer le produit par ID
        mockMvc.perform(get("/api/v1/products/" + produitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Carottes Bio"))
                .andExpect(jsonPath("$.bio").value(true))
                .andExpect(jsonPath("$.categorie").value("LEGUMES"));
    }

    @Test
    void getProduitById_NotFound_ShouldReturnError() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/products/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchProduits_Success() throws Exception {
        // Créer des produits de test
        createTestProduits();

        // Rechercher des produits
        mockMvc.perform(get("/api/v1/products/search")
                        .param("categorie", "LEGUMES")
                        .param("bio", "true")
                        .param("prixMin", "1.00")
                        .param("prixMax", "5.00")
                        .param("nom", "tomate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getProduitsPopulaires_Success() throws Exception {
        // Créer des produits de test
        createTestProduits();

        mockMvc.perform(get("/api/v1/products/popular")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getProduitsRecents_Success() throws Exception {
        // Créer des produits de test
        createTestProduits();

        mockMvc.perform(get("/api/v1/products/recent")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void verifierDisponibilite_Success() throws Exception {
        // Créer un produit
        ProduitRequest request = ProduitRequest.builder()
                .nom("Miel de Lavande")
                .description("Miel artisanal")
                .prix(new BigDecimal("8.90"))
                .quantiteDisponible(30)
                .categorie(CategorieProduit.MIEL)
                .unite("pot 500g")
                .build();

        String response = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + producteurToken)
                        .param("producteurId", producteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String produitId = objectMapper.readTree(response).get("id").asText();

        // Vérifier la disponibilité
        mockMvc.perform(get("/api/v1/products/" + produitId + "/availability")
                        .param("quantiteDemandee", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Vérifier la disponibilité avec une quantité trop élevée
        mockMvc.perform(get("/api/v1/products/" + produitId + "/availability")
                        .param("quantiteDemandee", "50"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    private void createTestProduits() throws Exception {
        // Produit 1
        ProduitRequest request1 = ProduitRequest.builder()
                .nom("Tomates Bio")
                .description("Tomates biologiques")
                .prix(new BigDecimal("4.50"))
                .quantiteDisponible(100)
                .categorie(CategorieProduit.LEGUMES)
                .unite("kg")
                .bio(true)
                .build();

        // Produit 2
        ProduitRequest request2 = ProduitRequest.builder()
                .nom("Pommes Golden")
                .description("Pommes Golden Delicious")
                .prix(new BigDecimal("3.20"))
                .quantiteDisponible(50)
                .categorie(CategorieProduit.FRUITS)
                .unite("kg")
                .bio(false)
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + producteurToken)
                        .param("producteurId", producteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + producteurToken)
                        .param("producteurId", producteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());
    }
} 