package com.cinnamon.backend.service;

import com.cinnamon.backend.model.OrderRequest;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FieldValue;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class OrderService {

    public String placeOrder(OrderRequest orderRequest) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", orderRequest.userId());
        orderData.put("items", orderRequest.items());
        orderData.put("contactNumber", orderRequest.contactNumber());
        orderData.put("notes", orderRequest.notes());
        orderData.put("paymentMethod", orderRequest.paymentMethod());
        orderData.put("paymentIntentId", orderRequest.paymentIntentId());
        orderData.put("totalAmount", orderRequest.totalAmount());
        orderData.put("amountCents", (int) Math.round(orderRequest.totalAmount() * 100));
        orderData.put("createdAt", FieldValue.serverTimestamp());
        orderData.put("pickupDate", orderRequest.pickupDate());
        orderData.put("pickupTime", orderRequest.pickupTime());
        orderData.put("status", "Pending");

        return db.collection("orders").add(orderData).get().getId();
    }
}
