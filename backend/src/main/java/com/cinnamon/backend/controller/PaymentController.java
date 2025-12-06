package com.cinnamon.backend.controller;

import com.cinnamon.backend.model.PaymentIntentRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Customer;
import com.stripe.model.EphemeralKey;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.EphemeralKeyCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Value("${stripe.secret.key}")
    private String secretKey;

    // Use the same Stripe API version across Mobile SDK and Ephemeral Key requests
    @Value("${stripe.api.version:2025-11-17.clover}")
    private String stripeApiVersion;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<String> createPaymentIntent(@RequestBody PaymentIntentRequest request) {
        try {
            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                            .setAmount(request.amount())
                            .setCurrency("usd")
                            .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            return ResponseEntity.ok(paymentIntent.getClientSecret());
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Mobile PaymentSheet bootstrap endpoint.
     * Returns: paymentIntent client secret, ephemeralKey secret, and a consistent customer id.
     * Ensures same Stripe API version is used with Ephemeral Keys as the Mobile SDK.
     */
    @PostMapping("/payment-sheet")
    public ResponseEntity<Map<String, String>> paymentSheet(@RequestBody Map<String, Object> body) {
        try {
            long amount = ((Number) body.getOrDefault("amount", 0)).longValue();
            String email = (String) body.getOrDefault("email", null);
            String existingCustomerId = (String) body.getOrDefault("customerId", null);

            // 1) Get or create a customer tied to the logged-in user
            String customerId = ensureCustomer(existingCustomerId, email);

            // 2) Create ephemeral key for the customer, using the same Stripe API version as the Mobile SDK
            EphemeralKeyCreateParams ekParams = EphemeralKeyCreateParams.builder()
                .setCustomer(customerId)
                .setStripeVersion(stripeApiVersion)
                .build();
            EphemeralKey ephemeralKey = EphemeralKey.create(ekParams);

            // 3) Create PaymentIntent for the given amount and customer
            PaymentIntentCreateParams piParams = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency("usd")
                    .setCustomer(customerId)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();
            PaymentIntent paymentIntent = PaymentIntent.create(piParams);

            Map<String, String> resp = new HashMap<>();
            resp.put("paymentIntent", paymentIntent.getClientSecret());
            resp.put("ephemeralKey", ephemeralKey.getSecret());
            resp.put("customer", customerId);
            return ResponseEntity.ok(resp);
        } catch (StripeException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Failed to prepare payment sheet: " + e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    private String ensureCustomer(String customerId, String email) throws StripeException {
        if (customerId != null && !customerId.isBlank()) {
            // Trust provided customer id; optionally verify exists
            return customerId;
        }
        if (email == null || email.isBlank()) {
            // Create anonymous customer if no email was provided
            CustomerCreateParams createParams = CustomerCreateParams.builder().build();
            Customer customer = Customer.create(createParams);
            return customer.getId();
        }
        // Try to find existing customer by email; Stripe API doesn't provide direct lookup by email,
        // in simple setups we create a customer with given email and reuse its id client-side for future calls.
        CustomerCreateParams createParams = CustomerCreateParams.builder()
                .setEmail(email)
                .build();
        Customer customer = Customer.create(createParams);
        return customer.getId();
    }
}
