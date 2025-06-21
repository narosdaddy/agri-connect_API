package com.cybernerd.agriConnect_APIBackend.integration;

import com.cybernerd.agriConnect_APIBackend.dtos.auth.LoginRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.auth.RegisterRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void registerUser_Success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .nom("Test User")
                .email("test@example.com")
                .motDePasse("password123")
                .role(Role.ACHETEUR)
                .telephone("+33123456789")
                .adresse("123 Test Street")
                .ville("Paris")
                .codePostal("75001")
                .pays("France")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nom").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("ACHETEUR"))
                .andExpect(jsonPath("$.emailVerifie").value(false))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void registerUser_DuplicateEmail_ShouldReturnError() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .nom("Test User")
                .email("test@example.com")
                .motDePasse("password123")
                .role(Role.ACHETEUR)
                .build();

        // Premier enregistrement
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Deuxième enregistrement avec le même email
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_InvalidData_ShouldReturnError() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .nom("") // Nom vide
                .email("invalid-email") // Email invalide
                .motDePasse("123") // Mot de passe trop court
                .role(Role.ACHETEUR)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerProducteur_Success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .nom("Test Producteur")
                .email("producteur@example.com")
                .motDePasse("password123")
                .role(Role.PRODUCTEUR)
                .nomExploitation("Ferme Test")
                .descriptionExploitation("Exploitation agricole de test")
                .certifieBio(true)
                .adresseExploitation("456 Farm Road")
                .villeExploitation("Lyon")
                .codePostalExploitation("69000")
                .paysExploitation("France")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomExploitation").value("Ferme Test"))
                .andExpect(jsonPath("$.certifieBio").value(true))
                .andExpect(jsonPath("$.role").value("PRODUCTEUR"));
    }

    @Test
    void loginUser_Success() throws Exception {
        // D'abord enregistrer un utilisateur
        RegisterRequest registerRequest = RegisterRequest.builder()
                .nom("Test User")
                .email("login@example.com")
                .motDePasse("password123")
                .role(Role.ACHETEUR)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Ensuite se connecter
        LoginRequest loginRequest = LoginRequest.builder()
                .email("login@example.com")
                .motDePasse("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.email").value("login@example.com"));
    }

    @Test
    void loginUser_InvalidCredentials_ShouldReturnError() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("nonexistent@example.com")
                .motDePasse("wrongpassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void isEmailVerified_ShouldReturnStatus() throws Exception {
        mockMvc.perform(get("/api/v1/auth/email-verified")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
} 