package com.cybernerd.agriConnect_APIBackend.service;

import com.cybernerd.agriConnect_APIBackend.dtos.produit.ProduitRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.produit.ProduitResponse;
import com.cybernerd.agriConnect_APIBackend.enumType.CategorieProduit;
import com.cybernerd.agriConnect_APIBackend.enumType.Role;
import com.cybernerd.agriConnect_APIBackend.model.Producteur;
import com.cybernerd.agriConnect_APIBackend.model.Produit;
import com.cybernerd.agriConnect_APIBackend.repository.ProducteurRepository;
import com.cybernerd.agriConnect_APIBackend.repository.ProduitRepository;
import com.cybernerd.agriConnect_APIBackend.serviceImpl.ProduitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProduitServiceTest {

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private ProducteurRepository producteurRepository;

    @InjectMocks
    private ProduitServiceImpl produitService;

    private Producteur producteur;
    private Produit produit;
    private ProduitRequest produitRequest;
    private UUID producteurId;
    private UUID produitId;

    @BeforeEach
    void setUp() {
        producteurId = UUID.randomUUID();
        produitId = UUID.randomUUID();

        producteur = Producteur.builder()
                .id(producteurId)
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
                .telephone("+33123456789")
                .adresse("123 Test Street")
                .ville("Paris")
                .codePostal("75001")
                .pays("France")
                .emailVerifie(true)
                .dateCreation(LocalDateTime.now())
                .noteMoyenne(4.5)
                .build();

        produit = Produit.builder()
                .id(produitId)
                .nom("Tomates Bio")
                .description("Tomates biologiques fraîches")
                .prix(new BigDecimal("4.50"))
                .quantiteDisponible(100)
                .categorie(CategorieProduit.LEGUMES)
                .unite("kg")
                .bio(true)
                .origine("Lyon")
                .imagePrincipale("image1.jpg")
                .producteur(producteur)
                .disponible(true)
                .noteMoyenne(new BigDecimal("4.2"))
                .nombreAvis(15)
                .build();

        produitRequest = ProduitRequest.builder()
                .nom("Tomates Bio")
                .description("Tomates biologiques fraîches")
                .prix(new BigDecimal("4.50"))
                .quantiteDisponible(100)
                .categorie(CategorieProduit.LEGUMES)
                .unite("kg")
                .bio(true)
                .origine("Lyon")
                .imagePrincipale("image1.jpg")
                .build();
    }

    @Test
    void creerProduit_Success() {
        // Given
        when(producteurRepository.findById(producteurId)).thenReturn(Optional.of(producteur));
        when(produitRepository.save(any(Produit.class))).thenReturn(produit);

        // When
        ProduitResponse result = produitService.creerProduit(produitRequest, producteurId);

        // Then
        assertNotNull(result);
        assertEquals("Tomates Bio", result.getNom());
        assertEquals(new BigDecimal("4.50"), result.getPrix());
        assertEquals(CategorieProduit.LEGUMES, result.getCategorie());
        assertEquals(true, result.isBio());
        assertEquals(producteurId, result.getProducteurId());
        assertEquals("Test Producteur", result.getNomProducteur());

        verify(producteurRepository).findById(producteurId);
        verify(produitRepository).save(any(Produit.class));
    }

    @Test
    void creerProduit_ProducteurNonTrouve_ShouldThrowException() {
        // Given
        when(producteurRepository.findById(producteurId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            produitService.creerProduit(produitRequest, producteurId);
        });

        verify(producteurRepository).findById(producteurId);
        verify(produitRepository, never()).save(any());
    }

    @Test
    void modifierProduit_Success() {
        // Given
        when(produitRepository.findById(produitId)).thenReturn(Optional.of(produit));
        when(produitRepository.save(any(Produit.class))).thenReturn(produit);

        ProduitRequest updateRequest = ProduitRequest.builder()
                .nom("Tomates Bio Premium")
                .description("Tomates biologiques premium")
                .prix(new BigDecimal("5.50"))
                .quantiteDisponible(80)
                .categorie(CategorieProduit.LEGUMES)
                .unite("kg")
                .bio(true)
                .origine("Lyon")
                .build();

        // When
        ProduitResponse result = produitService.modifierProduit(produitId, updateRequest, producteurId);

        // Then
        assertNotNull(result);
        assertEquals("Tomates Bio Premium", result.getNom());
        assertEquals(new BigDecimal("5.50"), result.getPrix());

        verify(produitRepository).findById(produitId);
        verify(produitRepository).save(any(Produit.class));
    }

    @Test
    void modifierProduit_ProduitNonTrouve_ShouldThrowException() {
        // Given
        when(produitRepository.findById(produitId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            produitService.modifierProduit(produitId, produitRequest, producteurId);
        });

        verify(produitRepository).findById(produitId);
        verify(produitRepository, never()).save(any());
    }

    @Test
    void modifierProduit_AccesNonAutorise_ShouldThrowException() {
        // Given
        UUID autreProducteurId = UUID.randomUUID();
        when(produitRepository.findById(produitId)).thenReturn(Optional.of(produit));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            produitService.modifierProduit(produitId, produitRequest, autreProducteurId);
        });

        verify(produitRepository).findById(produitId);
        verify(produitRepository, never()).save(any());
    }

    @Test
    void supprimerProduit_Success() {
        // Given
        when(produitRepository.findById(produitId)).thenReturn(Optional.of(produit));
        when(produitRepository.save(any(Produit.class))).thenReturn(produit);

        // When
        produitService.supprimerProduit(produitId, producteurId);

        // Then
        verify(produitRepository).findById(produitId);
        verify(produitRepository).save(any(Produit.class));
        assertFalse(produit.isDisponible());
    }

    @Test
    void getProduitById_Success() {
        // Given
        when(produitRepository.findById(produitId)).thenReturn(Optional.of(produit));

        // When
        ProduitResponse result = produitService.getProduitById(produitId);

        // Then
        assertNotNull(result);
        assertEquals(produitId, result.getId());
        assertEquals("Tomates Bio", result.getNom());
        assertEquals(new BigDecimal("4.50"), result.getPrix());

        verify(produitRepository).findById(produitId);
    }

    @Test
    void getProduitById_ProduitNonTrouve_ShouldThrowException() {
        // Given
        when(produitRepository.findById(produitId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            produitService.getProduitById(produitId);
        });

        verify(produitRepository).findById(produitId);
    }

    @Test
    void getAllProduits_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Produit> produits = Arrays.asList(produit);
        Page<Produit> page = new PageImpl<>(produits, pageable, 1);
        when(produitRepository.findByDisponibleTrue(pageable)).thenReturn(page);

        // When
        Page<ProduitResponse> result = produitService.getAllProduits(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Tomates Bio", result.getContent().get(0).getNom());

        verify(produitRepository).findByDisponibleTrue(pageable);
    }

    @Test
    void rechercherProduits_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Produit> produits = Arrays.asList(produit);
        Page<Produit> page = new PageImpl<>(produits, pageable, 1);
        when(produitRepository.rechercherProduits(
                eq(CategorieProduit.LEGUMES),
                eq(true),
                any(BigDecimal.class),
                any(BigDecimal.class),
                eq("tomate"),
                eq("Lyon"),
                eq(pageable)
        )).thenReturn(page);

        // When
        Page<ProduitResponse> result = produitService.rechercherProduits(
                CategorieProduit.LEGUMES,
                true,
                new BigDecimal("1.00"),
                new BigDecimal("10.00"),
                "tomate",
                "Lyon",
                pageable
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(produitRepository).rechercherProduits(
                eq(CategorieProduit.LEGUMES),
                eq(true),
                any(BigDecimal.class),
                any(BigDecimal.class),
                eq("tomate"),
                eq("Lyon"),
                eq(pageable)
        );
    }

    @Test
    void getProduitsByProducteur_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Produit> produits = Arrays.asList(produit);
        Page<Produit> page = new PageImpl<>(produits, pageable, 1);
        when(produitRepository.findByProducteurId(producteurId, pageable)).thenReturn(page);

        // When
        Page<ProduitResponse> result = produitService.getProduitsByProducteur(producteurId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(producteurId, result.getContent().get(0).getProducteurId());

        verify(produitRepository).findByProducteurId(producteurId, pageable);
    }

    @Test
    void uploadImage_Success() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(produitRepository.findById(produitId)).thenReturn(Optional.of(produit));
        when(produitRepository.save(any(Produit.class))).thenReturn(produit);

        // When
        String result = produitService.uploadImage(file, produitId, producteurId);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/produits/"));

        verify(produitRepository).findById(produitId);
        verify(produitRepository).save(any(Produit.class));
    }

    @Test
    void uploadImage_ProduitNonTrouve_ShouldThrowException() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(produitRepository.findById(produitId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            produitService.uploadImage(file, produitId, producteurId);
        });

        verify(produitRepository).findById(produitId);
        verify(produitRepository, never()).save(any());
    }

    @Test
    void mettreAJourStock_Success() {
        // Given
        when(produitRepository.findById(produitId)).thenReturn(Optional.of(produit));
        when(produitRepository.save(any(Produit.class))).thenReturn(produit);

        // When
        produitService.mettreAJourStock(produitId, 50, producteurId);

        // Then
        assertEquals(50, produit.getQuantiteDisponible());
        verify(produitRepository).findById(produitId);
        verify(produitRepository).save(any(Produit.class));
    }

    @Test
    void mettreAJourStock_QuantiteNegative_ShouldThrowException() {
        // Given
        when(produitRepository.findById(produitId)).thenReturn(Optional.of(produit));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            produitService.mettreAJourStock(produitId, -10, producteurId);
        });

        verify(produitRepository).findById(produitId);
        verify(produitRepository, never()).save(any());
    }

    @Test
    void verifierDisponibilite_Success() {
        // Given
        when(produitRepository.findById(produitId)).thenReturn(Optional.of(produit));

        // When
        boolean result = produitService.verifierDisponibilite(produitId, 50);

        // Then
        assertTrue(result);

        verify(produitRepository).findById(produitId);
    }

    @Test
    void verifierDisponibilite_QuantiteInsuffisante_ShouldReturnFalse() {
        // Given
        when(produitRepository.findById(produitId)).thenReturn(Optional.of(produit));

        // When
        boolean result = produitService.verifierDisponibilite(produitId, 150);

        // Then
        assertFalse(result);

        verify(produitRepository).findById(produitId);
    }

    @Test
    void verifierDisponibilite_ProduitNonDisponible_ShouldReturnFalse() {
        // Given
        produit.setDisponible(false);
        when(produitRepository.findById(produitId)).thenReturn(Optional.of(produit));

        // When
        boolean result = produitService.verifierDisponibilite(produitId, 50);

        // Then
        assertFalse(result);

        verify(produitRepository).findById(produitId);
    }
} 