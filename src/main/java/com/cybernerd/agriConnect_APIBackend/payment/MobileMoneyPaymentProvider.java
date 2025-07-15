package com.cybernerd.agriConnect_APIBackend.payment;

import com.cybernerd.agriConnect_APIBackend.dtos.commande.PaiementRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.commande.PaiementResponse;
import com.cybernerd.agriConnect_APIBackend.enumType.MethodePaiement;
import org.springframework.stereotype.Component;

@Component
public class MobileMoneyPaymentProvider implements PaymentProvider {
    @Override
    public boolean supports(MethodePaiement methode) {
        return MethodePaiement.MOBILE_MONEY.equals(methode);
    }

    @Override
    public PaiementResponse processPayment(PaiementRequest request) {
        // Ici tu appelleras l'API Mobile Money réelle
        return PaiementResponse.builder()
                .montant(request.getMontant())
                .methode(request.getMethode())
                .statut("SUCCES_MOBILE_MONEY")
                .build();
    }
} 