package com.cybernerd.agriConnect_APIBackend.service;

import com.cybernerd.agriConnect_APIBackend.dtos.commande.CommandeRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.commande.CommandeResponse;
import com.cybernerd.agriConnect_APIBackend.dtos.panier.PanierResponse;
import com.cybernerd.agriConnect_APIBackend.enumType.MethodePaiement;
import com.cybernerd.agriConnect_APIBackend.enumType.StatutCommande;
import com.cybernerd.agriConnect_APIBackend.model.Acheteur;
import com.cybernerd.agriConnect_APIBackend.model.Commande;
import com.cybernerd.agriConnect_APIBackend.model.ElementCommande;
import com.cybernerd.agriConnect_APIBackend.model.Produit;
import com.cybernerd.agriConnect_APIBackend.repository.AcheteurRepository;
import com.cybernerd.agriConnect_APIBackend.repository.CommandeRepository;
import com.cybernerd.agriConnect_APIBackend.repository.ElementCommandeRepository;
import com.cybernerd.agriConnect_APIBackend.repository.ProduitRepository;
import com.cybernerd.agriConnect_APIBackend.serviceImpl.CommandeServiceImpl;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandeServiceTest {

    @Mock
    private CommandeRepository commandeRepository;

    @Mock
    private ElementCommandeRepository elementCommandeRepository;

    @Mock
    private AcheteurRepository acheteurRepository;

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private PanierService panierService;

    @InjectMocks
    private CommandeServiceImpl commandeService;

    private Acheteur acheteur;
    private Produit produit;
    private Commande commande;
    private ElementCommande elementCommande;
    private CommandeRequest commandeRequest;
    private PanierResponse panierResponse;
    private UUID acheteurId;
    private UUID produitId;
    private UUID commandeId;
    private UUID elementCommandeId;

    @BeforeEach
    void setUp() {
        acheteurId = UUID.randomUUID();
        produitId = UUID.randomUUID();
        commandeId = UUID.randomUUID();
        elementCommandeId = UUID.randomUUID();

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

        elementCommande = ElementCommande.builder()
                .id(elementCommandeId)
                .produit(produit)
                .quantite(5)
                .prixUnitaire(new BigDecimal("4.50"))
                .prixTotal(new BigDecimal("22.50"))
                .build();

        commande = Commande.builder()
                .id(commandeId)
                .numeroCommande("CMD-20241201-001")
                .acheteur(acheteur)
                .statut(StatutCommande.EN_ATTENTE)
                .methodePaiement(MethodePaiement.CARTE_BANCAIRE)
                .sousTotal(new BigDecimal("22.50"))
                .fraisLivraison(new BigDecimal("5.00"))
                .remise(BigDecimal.ZERO)
                .total(new BigDecimal("27.50"))
                .dateCreation(LocalDateTime.now())
                .elements(Arrays.asList(elementCommande))
                .nombreElements(1)
                .build();

        commandeRequest = CommandeRequest.builder()
                .adresseLivraison("123 Rue de la Paix")
                .villeLivraison("Paris")
                .codePostalLivraison("75001")
                .paysLivraison("France")
                .telephoneLivraison("+33123456789")
                .instructionsLivraison("Livrer entre 14h et 18h")
                .methodePaiement(MethodePaiement.CARTE_BANCAIRE)
                .codePromo("BIENVENUE10")
                .elements(Arrays.asList(
                        CommandeRequest.ElementCommandeRequest.builder()
                                .produitId(produitId.toString())
                                .quantite(5)
                                .prixUnitaire(4.50)
                                .build()
                ))
                .build();

        panierResponse = PanierResponse.builder()
                .id(UUID.randomUUID())
                .elements(Arrays.asList(
                        PanierResponse.ElementPanierResponse.builder()
                                .produitId(produitId)
                                .nomProduit("Tomates Bio")
                                .quantite(5)
                                .prixUnitaire(new BigDecimal("4.50"))
                                .prixTotal(new BigDecimal("22.50"))
                                .disponible(true)
                                .build()
                ))
                .sousTotal(new BigDecimal("22.50"))
                .fraisLivraison(new BigDecimal("5.00"))
                .remise(BigDecimal.ZERO)
                .total(new BigDecimal("27.50"))
                .vide(false)
                .build();
    }

    @Test
    void creerCommande_Success() {
        // Given
        when(acheteurRepository.findById(acheteurId)).thenReturn(Optional.of(acheteur));
        when(panierService.getPanier(acheteurId)).thenReturn(panierResponse);
        when(panierService.verifierDisponibilitePanier(acheteurId)).thenReturn(true);
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);
        when(elementCommandeRepository.saveAll(anyList())).thenReturn(Arrays.asList(elementCommande));

        // When
        CommandeResponse result = commandeService.creerCommande(commandeRequest, acheteurId);

        // Then
        assertNotNull(result);
        assertEquals(commandeId, result.getId());
        assertEquals("CMD-20241201-001", result.getNumeroCommande());
        assertEquals(acheteurId, result.getAcheteurId());
        assertEquals(StatutCommande.EN_ATTENTE, result.getStatut());
        assertEquals(MethodePaiement.CARTE_BANCAIRE, result.getMethodePaiement());
        assertEquals(new BigDecimal("27.50"), result.getTotal());

        verify(acheteurRepository).findById(acheteurId);
        verify(panierService).getPanier(acheteurId);
        verify(panierService).verifierDisponibilitePanier(acheteurId);
        verify(commandeRepository).save(any(Commande.class));
        verify(elementCommandeRepository).saveAll(anyList());
        verify(panierService).viderPanier(acheteurId);
    }

    @Test
    void creerCommande_AcheteurNonTrouve_ShouldThrowException() {
        // Given
        when(acheteurRepository.findById(acheteurId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            commandeService.creerCommande(commandeRequest, acheteurId);
        });

        verify(acheteurRepository).findById(acheteurId);
        verify(panierService, never()).getPanier(any());
    }

    @Test
    void creerCommande_PanierVide_ShouldThrowException() {
        // Given
        PanierResponse panierVide = PanierResponse.builder()
                .vide(true)
                .build();

        when(acheteurRepository.findById(acheteurId)).thenReturn(Optional.of(acheteur));
        when(panierService.getPanier(acheteurId)).thenReturn(panierVide);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            commandeService.creerCommande(commandeRequest, acheteurId);
        });

        verify(acheteurRepository).findById(acheteurId);
        verify(panierService).getPanier(acheteurId);
        verify(panierService, never()).verifierDisponibilitePanier(any());
    }

    @Test
    void creerCommande_ProduitsNonDisponibles_ShouldThrowException() {
        // Given
        when(acheteurRepository.findById(acheteurId)).thenReturn(Optional.of(acheteur));
        when(panierService.getPanier(acheteurId)).thenReturn(panierResponse);
        when(panierService.verifierDisponibilitePanier(acheteurId)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            commandeService.creerCommande(commandeRequest, acheteurId);
        });

        verify(acheteurRepository).findById(acheteurId);
        verify(panierService).getPanier(acheteurId);
        verify(panierService).verifierDisponibilitePanier(acheteurId);
        verify(commandeRepository, never()).save(any());
    }

    @Test
    void getCommandeById_Success() {
        // Given
        when(commandeRepository.findById(commandeId)).thenReturn(Optional.of(commande));

        // When
        CommandeResponse result = commandeService.getCommandeById(commandeId);

        // Then
        assertNotNull(result);
        assertEquals(commandeId, result.getId());
        assertEquals("CMD-20241201-001", result.getNumeroCommande());
        assertEquals(acheteurId, result.getAcheteurId());

        verify(commandeRepository).findById(commandeId);
    }

    @Test
    void getCommandeById_CommandeNonTrouvee_ShouldThrowException() {
        // Given
        when(commandeRepository.findById(commandeId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            commandeService.getCommandeById(commandeId);
        });

        verify(commandeRepository).findById(commandeId);
    }

    @Test
    void getCommandesByAcheteur_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Commande> page = new PageImpl<>(Arrays.asList(commande), pageable, 1);
        when(commandeRepository.findByAcheteurIdOrderByDateCreationDesc(acheteurId, pageable)).thenReturn(page);

        // When
        Page<CommandeResponse> result = commandeService.getCommandesByAcheteur(acheteurId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(acheteurId, result.getContent().get(0).getAcheteurId());

        verify(commandeRepository).findByAcheteurIdOrderByDateCreationDesc(acheteurId, pageable);
    }

    @Test
    void mettreAJourStatut_Success() {
        // Given
        UUID producteurId = UUID.randomUUID();
        when(commandeRepository.findById(commandeId)).thenReturn(Optional.of(commande));
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // When
        CommandeResponse result = commandeService.mettreAJourStatut(commandeId, StatutCommande.CONFIRMEE, producteurId);

        // Then
        assertNotNull(result);
        assertEquals(StatutCommande.CONFIRMEE, result.getStatut());

        verify(commandeRepository).findById(commandeId);
        verify(commandeRepository).save(any(Commande.class));
    }

    @Test
    void mettreAJourStatut_CommandeNonTrouvee_ShouldThrowException() {
        // Given
        UUID producteurId = UUID.randomUUID();
        when(commandeRepository.findById(commandeId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            commandeService.mettreAJourStatut(commandeId, StatutCommande.CONFIRMEE, producteurId);
        });

        verify(commandeRepository).findById(commandeId);
        verify(commandeRepository, never()).save(any());
    }

    @Test
    void mettreAJourStatut_AccesNonAutorise_ShouldThrowException() {
        // Given
        UUID autreProducteurId = UUID.randomUUID();
        when(commandeRepository.findById(commandeId)).thenReturn(Optional.of(commande));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            commandeService.mettreAJourStatut(commandeId, StatutCommande.CONFIRMEE, autreProducteurId);
        });

        verify(commandeRepository).findById(commandeId);
        verify(commandeRepository, never()).save(any());
    }

    @Test
    void annulerCommande_Success() {
        // Given
        when(commandeRepository.findById(commandeId)).thenReturn(Optional.of(commande));
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // When
        CommandeResponse result = commandeService.annulerCommande(commandeId, acheteurId);

        // Then
        assertNotNull(result);
        assertEquals(StatutCommande.ANNULEE, result.getStatut());

        verify(commandeRepository).findById(commandeId);
        verify(commandeRepository).save(any(Commande.class));
    }

    @Test
    void annulerCommande_CommandeNonTrouvee_ShouldThrowException() {
        // Given
        when(commandeRepository.findById(commandeId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            commandeService.annulerCommande(commandeId, acheteurId);
        });

        verify(commandeRepository).findById(commandeId);
        verify(commandeRepository, never()).save(any());
    }

    @Test
    void annulerCommande_AccesNonAutorise_ShouldThrowException() {
        // Given
        UUID autreAcheteurId = UUID.randomUUID();
        when(commandeRepository.findById(commandeId)).thenReturn(Optional.of(commande));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            commandeService.annulerCommande(commandeId, autreAcheteurId);
        });

        verify(commandeRepository).findById(commandeId);
        verify(commandeRepository, never()).save(any());
    }

    @Test
    void peutEtreAnnulee_CommandeEnAttente_ShouldReturnTrue() {
        // Given
        commande.setStatut(StatutCommande.EN_ATTENTE);
        when(commandeRepository.findById(commandeId)).thenReturn(Optional.of(commande));

        // When
        boolean result = commandeService.peutEtreAnnulee(commandeId);

        // Then
        assertTrue(result);

        verify(commandeRepository).findById(commandeId);
    }

    @Test
    void peutEtreAnnulee_CommandeConfirmee_ShouldReturnTrue() {
        // Given
        commande.setStatut(StatutCommande.CONFIRMEE);
        when(commandeRepository.findById(commandeId)).thenReturn(Optional.of(commande));

        // When
        boolean result = commandeService.peutEtreAnnulee(commandeId);

        // Then
        assertTrue(result);

        verify(commandeRepository).findById(commandeId);
    }

    @Test
    void peutEtreAnnulee_CommandeEnPreparation_ShouldReturnFalse() {
        // Given
        commande.setStatut(StatutCommande.EN_PREPARATION);
        when(commandeRepository.findById(commandeId)).thenReturn(Optional.of(commande));

        // When
        boolean result = commandeService.peutEtreAnnulee(commandeId);

        // Then
        assertFalse(result);

        verify(commandeRepository).findById(commandeId);
    }

    @Test
    void getCommandesRecentes_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Commande> page = new PageImpl<>(Arrays.asList(commande), pageable, 1);
        when(commandeRepository.findByOrderByDateCreationDesc(pageable)).thenReturn(page);

        // When
        Page<CommandeResponse> result = commandeService.getCommandesRecentes(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(commandeRepository).findByOrderByDateCreationDesc(pageable);
    }

    @Test
    void getAnalyticsProducteur_Success() {
        // Given
        UUID producteurId = UUID.randomUUID();
        when(commandeRepository.countByProducteurAndPeriode(eq(producteurId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(10L);
        when(commandeRepository.calculerChiffreAffaires(eq(producteurId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("500.00"));

        // When
        Object result = commandeService.getAnalyticsProducteur(producteurId, "30j");

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);

        verify(commandeRepository).countByProducteurAndPeriode(eq(producteurId), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(commandeRepository).calculerChiffreAffaires(eq(producteurId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getStatistiquesCommandes_Success() {
        // Given
        UUID producteurId = UUID.randomUUID();
        when(commandeRepository.countByProducteurId(producteurId)).thenReturn(50L);
        when(commandeRepository.calculerChiffreAffairesTotal(producteurId)).thenReturn(new BigDecimal("2500.00"));
        when(commandeRepository.countByProducteurIdAndStatutIn(eq(producteurId), anyList())).thenReturn(15L);

        // When
        Object result = commandeService.getStatistiquesCommandes(producteurId);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);

        verify(commandeRepository).countByProducteurId(producteurId);
        verify(commandeRepository).calculerChiffreAffairesTotal(producteurId);
        verify(commandeRepository).countByProducteurIdAndStatutIn(eq(producteurId), anyList());
    }
} 