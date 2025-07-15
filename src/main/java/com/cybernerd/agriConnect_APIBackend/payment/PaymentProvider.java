package com.cybernerd.agriConnect_APIBackend.payment;

import com.cybernerd.agriConnect_APIBackend.dtos.commande.PaiementRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.commande.PaiementResponse;
import com.cybernerd.agriConnect_APIBackend.enumType.MethodePaiement;

public interface PaymentProvider {
    boolean supports(MethodePaiement methode);
    PaiementResponse processPayment(PaiementRequest request);
} 