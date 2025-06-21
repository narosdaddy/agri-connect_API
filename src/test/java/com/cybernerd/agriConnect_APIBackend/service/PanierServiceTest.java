package com.cybernerd.agriConnect_APIBackend.service;

import com.cybernerd.agriConnect_APIBackend.dtos.panier.ElementPanierRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.panier.PanierResponse;
import com.cybernerd.agriConnect_APIBackend.model.Acheteur;
import com.cybernerd.agriConnect_APIBackend.model.ElementPanier;
import com.cybernerd.agriConnect_APIBackend.model.Panier;
import com.cybernerd.agriConnect_APIBackend.model.Produit;
import com.cybernerd.agriConnect_APIBackend.repository.AcheteurRepository;
import com.cybernerd.agriConnect_APIBackend.repository.ElementPanierRepository;
import com.cybernerd.agriConnect_APIBackend.repository.PanierRepository;
import com.cybernerd.agriConnect_APIBackend.repository.ProduitRepository;
import com.cybernerd.agriConnect_APIBackend.serviceImpl.PanierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PanierServiceTest {

    @Mock
    private PanierRepository panierRepository;

    @Mock
    private ElementPanierRepository elementPanierRepository;

    @Mock
    private AcheteurRepository acheteurRepository;

    @Mock
    private ProduitRepository produitRepository;

    @InjectMocks
    private PanierServiceImpl panierService;

    private Acheteur acheteur;
    private Produit produit;
    private Panier panier;
    private ElementPanier elementPanier;
    private ElementPanierRequest elementPanierRequest;
    private UUID acheteurId;
    private UUID produitId;
    private UUID panierId;
    private UUID elementPanierId;

    @BeforeEach
    void setUp() {
        acheteurId = UUID.randomUUID();
        produitId = UUID.randomUUID();
        panierId = UUID.randomUUID();
        elementPanierId = UUID.randomUUID();

        acheteur = Acheteur.builder()
                .id(acheteurId)
                .nom("Test Acheteur")
                .email("acheteur@test.com")
                .build();

        produit = Produit.builder()
                .id(produitId)
                .nom("Tomates Bio")
                .prix(new BigDecimal("4.50"))
                .quantiteDisponible(100)
                .unite("kg")
                .disponible(true)
                .build();

        panier = Panier.builder()
                .id(panierId)
                .acheteur(acheteur)
                .elements(new ArrayList<>())
                .sousTotal(BigDecimal.ZERO)
                .fraisLivraison(BigDecimal.ZERO)
                .remise(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .build();

        elementPanier = ElementPanier.builder()
                .id(elementPanierId)
                .panier(panier)
                .produit(produit)
                .quantite(5)
                .prixUnitaire(new BigDecimal("4.50"))
                .prixTotal(new BigDecimal("22.50"))
                .build();

        elementPanierRequest = ElementPanierRequest.builder()
                .produitId(produitId)
                .quantite(5)
                .build();
    }

    @Test
    void getPanier_PanierExistant_Success() {
        // Given
        when(panierRepository.findByAcheteurId(acheteurId)).thenReturn(Optional.of(panier));

        // When
        PanierResponse result = panierService.getPanier(acheteurId);

        // Then
        assertNotNull(result);
        assertEquals(panierId, result.getId());
        assertTrue(result.isVide());

        verify(panierRepository).findByAcheteurId(acheteurId);
    }

    @Test
    void getPanier_PanierInexistant_CreateNewPanier() {
        // Given
        when(panierRepository.findByAcheteurId(acheteurId)).thenReturn(Optional.empty());
        when(acheteurRepository.findById(acheteurId)).thenReturn(Optional.of(acheteur));
        when(panierRepository.save(any(Panier.class))).thenReturn(panier);

        // When
        PanierResponse result = panierService.getPanier(acheteurId);

        // Then
        assertNotNull(result);
        assertEquals(panierId, result.getId());

        verify(panierRepository).findByAcheteurId(acheteurId);
        verify(acheteurRepository).findById(acheteurId);
        verify(panierRepository).save(any(Panier.class));
    }

    @Test
    void ajouterElement_Success() {
        // Given
        when(panierRepository.findByAcheteurId(acheteurId)).thenReturn(Optional.of(panier));
        when(produitRepository.findById(produitId)).thenReturn(Optional.of(produit));
        when(elementPanierRepository.save(any(ElementPanier.class))).thenReturn(elementPanier);
        when(panierRepository.save(any(Panier.class))).thenReturn(panier);

        // When
        PanierResponse result = panierService.ajouterElement(acheteurId, elementPanierRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getElements().size());
        assertEquals(new BigDecimal("22.50"), result.getSousTotal());
        assertEquals(new BigDecimal("22.50"), result.getTotal());

        verify(panierRepository).findByAcheteurId(acheteurId);
        verify(produitRepository).findById(produitId);
        verify(elementPanierRepository).save(any(ElementPanier.class));
        verify(panierRepository).save(any(Panier.class));
    }

    @Test
    void ajouterElement_ProduitNonDisponible_ShouldThrowException() {
        // Given
        produit.setDisponible(false);
        when(panierRepository.findByAcheteurId(acheteurId)).thenReturn(Optional.of(panier));
        when(produitRepository.findById(produitId)).thenReturn(Optional.of(produit));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            panierService.ajouterElement(acheteurId, elementPanierRequest);
        });

        verify(panierRepository).findByAcheteurId(acheteurId);
        verify(produitRepository).findById(produitId);
        verify(elementPanierRepository, never()).save(any());
    }

    @Test
    void ajouterElement_QuantiteInsuffisante_ShouldThrowException() {
        // Given
        ElementPanierRequest request = ElementPanierRequest.builder()
                .produitId(produitId)
                .quantite(150) // Plus que disponible
                .build();

        when(panierRepository.findByAcheteurId(acheteurId)).thenReturn(Optional.of(panier));
        when(produitRepository.findById(produitId)).thenReturn(Optional.of(produit));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            panierService.ajouterElement(acheteurId, request);
        });

        verify(panierRepository).findByAcheteurId(acheteurId);
        verify(produitRepository).findById(produitId);
        verify(elementPanierRepository, never()).save(any());
    }

    @Test
    void ajouterElement_ElementExistant_UpdateQuantite() {
        // Given
        panier.getElements().add(elementPanier);
        when(panierRepository.findByAcheteurId(acheteurId)).thenReturn(Optional.of(panier));
        when(produitRepository.findById(produitId)).thenReturn(Optional.of(produit));
        when(elementPanierRepository.save(any(ElementPanier.class))).thenReturn(elementPanier);
        when(panierRepository.save(any(Panier.class))).thenReturn(panier);

        // When
        PanierResponse result = panierService.ajouterElement(acheteurId, elementPanierRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getElements().size());

        verify(panierRepository).findByAcheteurId(acheteurId);
        verify(produitRepository).findById(produitId);
        verify(elementPanierRepository).save(any(ElementPanier.class));
        verify(panierRepository).save(any(Panier.class));
    }

    @Test
    void modifierQuantite_Success() {
        // Given
        when(elementPanierRepository.findById(elementPanierId)).thenReturn(Optional.of(elementPanier));
        when(elementPanierRepository.save(any(ElementPanier.class))).thenReturn(elementPanier);
        when(panierRepository.save(any(Panier.class))).thenReturn(panier);

        // When
        PanierResponse result = panierService.modifierQuantite(acheteurId, elementPanierId, 10);

        // Then
        assertNotNull(result);
        assertEquals(10, elementPanier.getQuantite());

        verify(elementPanierRepository).findById(elementPanierId);
        verify(elementPanierRepository).save(any(ElementPanier.class));
        verify(panierRepository).save(any(Panier.class));
    }

    @Test
    void modifierQuantite_QuantiteZero_ShouldDeleteElement() {
        // Given
        when(elementPanierRepository.findById(elementPanierId)).thenReturn(Optional.of(elementPanier));

        // When
        PanierResponse result = panierService.modifierQuantite(acheteurId, elementPanierId, 0);

        // Then
        assertNotNull(result);
        assertTrue(result.isVide());

        verify(elementPanierRepository).findById(elementPanierId);
        verify(elementPanierRepository).delete(elementPanier);
        verify(panierRepository).save(any(Panier.class));
    }

    @Test
    void modifierQuantite_AccesNonAutorise_ShouldThrowException() {
        // Given
        UUID autreAcheteurId = UUID.randomUUID();
        when(elementPanierRepository.findById(elementPanierId)).thenReturn(Optional.of(elementPanier));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            panierService.modifierQuantite(autreAcheteurId, elementPanierId, 10);
        });

        verify(elementPanierRepository).findById(elementPanierId);
        verify(elementPanierRepository, never()).save(any());
    }

    @Test
    void supprimerElement_Success() {
        // Given
        when(elementPanierRepository.findById(elementPanierId)).thenReturn(Optional.of(elementPanier));

        // When
        PanierResponse result = panierService.supprimerElement(acheteurId, elementPanierId);

        // Then
        assertNotNull(result);
        assertTrue(result.isVide());

        verify(elementPanierRepository).findById(elementPanierId);
        verify(elementPanierRepository).delete(elementPanier);
        verify(panierRepository).save(any(Panier.class));
    }

    @Test
    void viderPanier_Success() {
        // Given
        panier.getElements().add(elementPanier);
        when(panierRepository.findByAcheteurId(acheteurId)).thenReturn(Optional.of(panier));
        when(panierRepository.save(any(Panier.class))).thenReturn(panier);

        // When
        PanierResponse result = panierService.viderPanier(acheteurId);

        // Then
        assertNotNull(result);
        assertTrue(result.isVide());
        assertEquals(0, result.getElements().size());

        verify(panierRepository).findByAcheteurId(acheteurId);
        verify(elementPanierRepository).deleteAll(panier.getElements());
        verify(panierRepository).save(any(Panier.class));
    }

    @Test
    void appliquerCodePromo_Success() {
        // Given
        when(panierRepository.findByAcheteurId(acheteurId)).thenReturn(Optional.of(panier));
        when(panierRepository.save(any(Panier.class))).thenReturn(panier);

        // When
        PanierResponse result = panierService.appliquerCodePromo(acheteurId, "BIENVENUE10");

        // Then
        assertNotNull(result);
        assertEquals("BIENVENUE10", result.getCodePromo());

        verify(panierRepository).findByAcheteurId(acheteurId);
        verify(panierRepository).save(any(Panier.class));
    }

    @Test
    void appliquerCodePromo_CodeInvalide_ShouldThrowException() {
        // Given
        when(panierRepository.findByAcheteurId(acheteurId)).thenReturn(Optional.of(panier));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            panierService.appliquerCodePromo(acheteurId, "INVALIDCODE");
        });

        verify(panierRepository).findByAcheteurId(acheteurId);
        verify(panierRepository, never()).save(any());
    }

    @Test
    void supprimerCodePromo_Success() {
        // Given
        panier.setCodePromo("BIENVENUE10");
        when(panierRepository.findByAcheteurId(acheteurId)).thenReturn(Optional.of(panier));
        when(panierRepository.save(any(Panier.class))).thenReturn(panier);

        // When
        PanierResponse result = panierService.supprimerCodePromo(acheteurId);

        // Then
        assertNotNull(result);
        assertNull(result.getCodePromo());

        verify(panierRepository).findByAcheteurId(acheteurId);
        verify(panierRepository).save(any(Panier.class));
    }

    @Test
    void verifierDisponibilitePanier_Success() {
        // Given
        panier.getElements().add(elementPanier);
        when(panierRepository.findByAcheteurId(acheteurId)).thenReturn(Optional.of(panier));

        // When
        boolean result = panierService.verifierDisponibilitePanier(acheteurId);

        // Then
        assertTrue(result);

        verify(panierRepository).findByAcheteurId(acheteurId);
    }

    @Test
    void verifierDisponibilitePanier_ProduitNonDisponible_ShouldReturnFalse() {
        // Given
        produit.setDisponible(false);
        panier.getElements().add(elementPanier);
        when(panierRepository.findByAcheteurId(acheteurId)).thenReturn(Optional.of(panier));

        // When
        boolean result = panierService.verifierDisponibilitePanier(acheteurId);

        // Then
        assertFalse(result);

        verify(panierRepository).findByAcheteurId(acheteurId);
    }

    @Test
    void verifierDisponibilitePanier_QuantiteInsuffisante_ShouldReturnFalse() {
        // Given
        elementPanier.setQuantite(150); // Plus que disponible
        panier.getElements().add(elementPanier);
        when(panierRepository.findByAcheteurId(acheteurId)).thenReturn(Optional.of(panier));

        // When
        boolean result = panierService.verifierDisponibilitePanier(acheteurId);

        // Then
        assertFalse(result);

        verify(panierRepository).findByAcheteurId(acheteurId);
    }
} 