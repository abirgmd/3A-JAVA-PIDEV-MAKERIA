package com.abircode.cruddp.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

public class PaiementService {

    private static PaiementService instance;

    private PaiementService() {
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }

    public static PaiementService getInstance() {
        if (instance == null) {
            instance = new PaiementService();
        }
        return instance;
    }

    public Session createCheckoutSession(double montantTND) throws StripeException {
        double tauxConversion = 0.30;
        double montantEUR = montantTND * tauxConversion;
        long amountInCents = Math.round(montantEUR * 100);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://example.com/success")
                .setCancelUrl("https://example.com/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Paiement Application JavaFX")
                                                                .build()
                                                )
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .build();

        return Session.create(params);
    }
}
