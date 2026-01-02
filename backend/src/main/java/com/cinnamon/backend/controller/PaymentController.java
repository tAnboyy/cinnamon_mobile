package com.cinnamon.backend.controller;

import com.cinnamon.backend.model.PaymentIntentRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Customer;
import com.stripe.model.EphemeralKey;
import com.stripe.param.PaymentIntentCreateParams;

import javax.annotation.PostConstruct;

import com.stripe.param.CustomerCreateParams;
import com.stripe.param.EphemeralKeyCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Value("${stripe.secret.key}")
    private String secretKey;

    // Use the same Stripe API version across Mobile SDK and Ephemeral Key requests
    // Using a stable, well-supported version
    @Value("${stripe.api.version:2024-06-20}")
    private String stripeApiVersion;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        logger.info("[PaymentController] Stripe API initialized with key: {}... and API version: {}", 
                   secretKey.substring(0, Math.min(10, secretKey.length())), stripeApiVersion);
    }

    public PaymentController() {
        Stripe.apiKey = secretKey;
    }

    /**
     * Mobile PaymentSheet bootstrap endpoint.
     * Returns: paymentIntent client secret, ephemeralKey secret, and a consistent customer id.
     * Ensures same Stripe API version is used with Ephemeral Keys as the Mobile SDK.
     */
    @PostMapping("/payment-sheet")
    public ResponseEntity<Map<String, String>> paymentSheet(@RequestBody Map<String, Object> body) {
        logger.info("[PaymentController] Payment sheet request received");
        
        try {
            long amount = ((Number) body.getOrDefault("amount", 0)).longValue();
            String email = (String) body.getOrDefault("email", null);
            String existingCustomerId = (String) body.getOrDefault("customerId", null);
            
            logger.info("[PaymentController] Processing payment request - amount: {}, email: {}, existingCustomerId: {}", 
                       amount, email, existingCustomerId != null ? existingCustomerId.substring(0, Math.min(10, existingCustomerId.length())) + "..." : "null");

            // 1) Get or create a customer tied to the logged-in user
            String customerId = ensureCustomer(existingCustomerId, email);
            logger.info("[PaymentController] Using customer ID: {}", customerId.substring(0, Math.min(10, customerId.length())) + "...");

            // 2) Create ephemeral key for the customer, using the same Stripe API version as the Mobile SDK
            logger.debug("[PaymentController] Creating ephemeral key with API version: {}", stripeApiVersion);
            EphemeralKeyCreateParams ekParams = EphemeralKeyCreateParams.builder()
                .setCustomer(customerId)
                .setStripeVersion(stripeApiVersion)
                .build();
            EphemeralKey ephemeralKey = EphemeralKey.create(ekParams);
            logger.info("[PaymentController] Ephemeral key created successfully");

            // 3) Create PaymentIntent with automatic payment methods enabled
            logger.debug("[PaymentController] Creating payment intent for amount: {} cents", amount);
            PaymentIntentCreateParams piParams = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency("usd")
                    .setCustomer(customerId)
                    .setAutomaticPaymentMethods(
                            // PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            //         .setEnabled(true)
                            //         .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.ALWAYS)
                            //         .build()
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                    )
                    .build();
            PaymentIntent paymentIntent = PaymentIntent.create(piParams);
            logger.info("[PaymentController] Payment intent created successfully: {}", 
                       paymentIntent.getId().substring(0, Math.min(10, paymentIntent.getId().length())) + "...");

            Map<String, String> resp = new HashMap<>();
            resp.put("paymentIntent", paymentIntent.getClientSecret());
            resp.put("ephemeralKey", ephemeralKey.getSecret());
            resp.put("customer", customerId);
            
            logger.info("[PaymentController] Payment sheet prepared successfully");
            return ResponseEntity.ok(resp);
        } catch (StripeException e) {
            logger.error("[PaymentController] Stripe error during payment sheet creation: {}", e.getMessage(), e);
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            logger.error("[PaymentController] Unexpected error during payment sheet creation: {}", e.getMessage(), e);
            Map<String, String> err = new HashMap<>();
            err.put("error", "Failed to prepare payment sheet: " + e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    private String ensureCustomer(String customerId, String email) throws StripeException {
        if (customerId != null && !customerId.isBlank()) {
            try {
                // Validate that the customer still exists
                Customer.retrieve(customerId);
                logger.debug("[PaymentController] Using existing customer ID: {}", customerId.substring(0, Math.min(10, customerId.length())) + "...");
                return customerId;
            } catch (StripeException e) {
                logger.warn("[PaymentController] Stored customer ID {} is invalid, creating new customer. Error: {}", 
                           customerId.substring(0, Math.min(10, customerId.length())) + "...", e.getMessage());
                // Fall through to create new customer
            }
        }
        if (email == null || email.isBlank()) {
            logger.info("[PaymentController] Creating anonymous customer (no email provided)");
            CustomerCreateParams createParams = CustomerCreateParams.builder().build();
            Customer customer = Customer.create(createParams);
            logger.info("[PaymentController] Anonymous customer created: {}", customer.getId().substring(0, Math.min(10, customer.getId().length())) + "...");
            return customer.getId();
        }
        logger.info("[PaymentController] Creating customer with email: {}", email);
        CustomerCreateParams createParams = CustomerCreateParams.builder()
                .setEmail(email)
                .build();
        Customer customer = Customer.create(createParams);
        logger.info("[PaymentController] Customer created: {}", customer.getId().substring(0, Math.min(10, customer.getId().length())) + "...");
        return customer.getId();
    }
}
