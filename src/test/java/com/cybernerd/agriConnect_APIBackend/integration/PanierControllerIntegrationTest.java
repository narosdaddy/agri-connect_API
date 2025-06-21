package com.cybernerd.agriConnect_APIBackend.integration;

import com.cybernerd.agriConnect_APIBackend.dtos.auth.AuthResponse;
import com.cybernerd.agriConnect_APIBackend.dtos.auth.LoginRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.auth.RegisterRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.panier.ElementPanierRequest;
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
class PanierControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String acheteurToken;
    private UUID acheteurId;
    private UUID produitId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        setupAcheteur();
        setupProduit();
    }

    private void setupAcheteur() throws Exception {
        // Enregistrer un acheteur
        RegisterRequest registerRequest = RegisterRequest.builder()
                .nom("Test Acheteur")
                .email("acheteur@example.com")
                .motDePasse("password123")
                .role(Role.ACHETEUR)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Se connecter pour obtenir le token
        LoginRequest loginRequest = LoginRequest.builder()
                .email("acheteur@example.com")
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
        acheteurToken = authResponse.getToken();
        acheteurId = authResponse.getId();
    }

    private void setupProduit() throws Exception {
        // Créer un producteur
        RegisterRequest producteurRequest = RegisterRequest.builder()
                .nom("Test Producteur")
                .email("producteur@example.com")
                .motDePasse("password123")
                .role(Role.PRODUCTEUR)
                .nomExploitation("Ferme Test")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(producteurRequest)))
                .andExpect(status().isCreated());

        // Se connecter en tant que producteur
        LoginRequest producteurLogin = LoginRequest.builder()
                .email("producteur@example.com")
                .motDePasse("password123")
                .build();

        String producteurResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(producteurLogin)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse producteurAuth = objectMapper.readValue(producteurResponse, AuthResponse.class);
        String producteurToken = producteurAuth.getToken();
        UUID producteurId = producteurAuth.getId();

        // Créer un produit
        ProduitRequest produitRequest = ProduitRequest.builder()
                .nom("Tomates Bio")
                .description("Tomates biologiques fraîches")
                .prix(new BigDecimal("4.50"))
                .quantiteDisponible(100)
                .categorie(CategorieProduit.LEGUMES)
                .unite("kg")
                .bio(true)
                .origine("Lyon")
                .build();

        String produitResponse = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + producteurToken)
                        .param("producteurId", producteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produitRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        produitId = UUID.fromString(objectMapper.readTree(produitResponse).get("id").asText());
    }

    @Test
    void getPanier_Success() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", "Bearer " + acheteurToken)
                        .param("acheteurId", acheteurId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elements").isArray())
                .andExpect(jsonPath("$.vide").value(true))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void ajouterElement_Success() throws Exception {
        ElementPanierRequest request = ElementPanierRequest.builder()
                .produitId(produitId)
                .quantite(5)
                .build();

        mockMvc.perform(post("/api/v1/cart/add")
                        .header("Authorization", "Bearer " + acheteurToken)
                        .param("acheteurId", acheteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elements").isArray())
                .andExpect(jsonPath("$.elements.length()").value(1))
                .andExpect(jsonPath("$.elements[0].nomProduit").value("Tomates Bio"))
                .andExpect(jsonPath("$.elements[0].quantite").value(5))
                .andExpect(jsonPath("$.elements[0].prixUnitaire").value(4.50))
                .andExpect(jsonPath("$.elements[0].prixTotal").value(22.50))
                .andExpect(jsonPath("$.sousTotal").value(22.50))
                .andExpect(jsonPath("$.total").value(22.50))
                .andExpect(jsonPath("$.vide").value(false));
    }

    @Test
    void ajouterElement_ProduitNonDisponible_ShouldReturnError() throws Exception {
        ElementPanierRequest request = ElementPanierRequest.builder()
                .produitId(produitId)
                .quantite(150) // Plus que disponible
                .build();

        mockMvc.perform(post("/api/v1/cart/add")
                        .header("Authorization", "Bearer " + acheteurToken)
                        .param("acheteurId", acheteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modifierQuantite_Success() throws Exception {
        // D'abord ajouter un élément
        ElementPanierRequest addRequest = ElementPanierRequest.builder()
                .produitId(produitId)
                .quantite(3)
                .build();

        String response = mockMvc.perform(post("/api/v1/cart/add")
                        .header("Authorization", "Bearer " + acheteurToken)
                        .param("acheteurId", acheteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extraire l'ID de l'élément
        String elementId = objectMapper.readTree(response).get("elements").get(0).get("id").asText();

        // Modifier la quantité
        mockMvc.perform(put("/api/v1/cart/update-quantity")
                        .header("Authorization", "Bearer " + acheteurToken)
                        .param("acheteurId", acheteurId.toString())
                        .param("elementId", elementId)
                        .param("nouvelleQuantite", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elements[0].quantite").value(7))
                .andExpect(jsonPath("$.elements[0].prixTotal").value(31.50))
                .andExpect(jsonPath("$.sousTotal").value(31.50));
    }

    @Test
    void supprimerElement_Success() throws Exception {
        // D'abord ajouter un élément
        ElementPanierRequest addRequest = ElementPanierRequest.builder()
                .produitId(produitId)
                .quantite(2)
                .build();

        String response = mockMvc.perform(post("/api/v1/cart/add")
                        .header("Authorization", "Bearer " + acheteurToken)
                        .param("acheteurId", acheteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String elementId = objectMapper.readTree(response).get("elements").get(0).get("id").asText();

        // Supprimer l'élément
        mockMvc.perform(delete("/api/v1/cart/remove")
                        .header("Authorization", "Bearer " + acheteurToken)
                        .param("acheteurId", acheteurId.toString())
                        .param("elementId", elementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elements").isArray())
                .andExpect(jsonPath("$.elements.length()").value(0))
                .andExpect(jsonPath("$.vide").value(true))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void viderPanier_Success() throws Exception {
        // D'abord ajouter des éléments
        ElementPanierRequest request = ElementPanierRequest.builder()
                .produitId(produitId)
                .quantite(3)
                .build();

        mockMvc.perform(post("/api/v1/cart/add")
                        .header("Authorization", "Bearer " + acheteurToken)
                        .param("acheteurId", acheteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Vider le panier
        mockMvc.perform(delete("/api/v1/cart/clear")
                        .header("Authorization", "Bearer " + acheteurToken)
                        .param("acheteurId", acheteurId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elements").isArray())
                .andExpect(jsonPath("$.elements.length()").value(0))
                .andExpect(jsonPath("$.vide").value(true))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void appliquerCodePromo_Success() throws Exception {
        // D'abord ajouter un élément
        ElementPanierRequest request = ElementPanierRequest.builder()
                .produitId(produitId)
                .quantite(4)
                .build();

        mockMvc.perform(post("/api/v1/cart/add")
                        .header("Authorization", "Bearer " + acheteurToken)
                        .param("acheteurId", acheteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Appliquer un code promo
        mockMvc.perform(post("/api/v1/cart/apply-promo")
                        .header("Authorization", "Bearer " + acheteurToken)
                        .param("acheteurId", acheteurId.toString())
                        .param("codePromo", "BIENVENUE10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codePromo").value("BIENVENUE10"))
                .andExpect(jsonPath("$.remise").value(1.80)) // 10% de 18.00
                .andExpect(jsonPath("$.total").value(16.20)); // 18.00 - 1.80
    }

    @Test
    void appliquerCodePromo_InvalidCode_ShouldReturnError() throws Exception {
        // D'abord ajouter un élément
        ElementPanierRequest request = ElementPanierRequest.builder()
                .produitId(produitId)
                .quantite(2)
                .build();

        mockMvc.perform(post("/api/v1/cart/add")
                        .header("Authorization", "Bearer " + acheteurToken)
                        .param("acheteurId", acheteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Appliquer un code promo invalide
        mockMvc.perform(post("/api/v1/cart/apply-promo")
                        .header("Authorization", "Bearer " + acheteurToken)
                        .param("acheteurId", acheteurId.toString())
                        .param("codePromo", "INVALIDCODE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifierDisponibilite_Success() throws Exception {
        // D'abord ajouter un élément
        ElementPanierRequest request = ElementPanierRequest.builder()
                .produitId(produitId)
                .quantite(5)
                .build();

        mockMvc.perform(post("/api/v1/cart/add")
                        .header("Authorization", "Bearer " + acheteurToken)
                        .param("acheteurId", acheteurId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Vérifier la disponibilité
        mockMvc.perform(get("/api/v1/cart/check-availability")
                        .header("Authorization", "Bearer " + acheteurToken)
                        .param("acheteurId", acheteurId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
} 