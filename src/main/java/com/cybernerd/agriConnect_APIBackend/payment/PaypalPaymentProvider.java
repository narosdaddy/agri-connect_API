package com.cybernerd.agriConnect_APIBackend.payment;

import com.cybernerd.agriConnect_APIBackend.dtos.commande.PaiementRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.commande.PaiementResponse;
import com.cybernerd.agriConnect_APIBackend.enumType.MethodePaiement;
import org.springframework.stereotype.Component;

@Component
public class PaypalPaymentProvider implements PaymentProvider {
    @Override
    public boolean supports(MethodePaiement methode) {
        return MethodePaiement.PAYPAL.equals(methode);
    }

    @Override
    public PaiementResponse processPayment(PaiementRequest request) {
        // Ici tu appelleras l'API PayPal réelle
        return PaiementResponse.builder()
                .montant(request.getMontant())
                .methode(request.getMethode())
                .statut("SUCCES_PAYPAL")
                .build();
    }
} 