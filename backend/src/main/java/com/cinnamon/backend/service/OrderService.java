package com.cinnamon.backend.service;

import com.cinnamon.backend.model.OrderRequest;
import com.google.cloud.firestore.Firestore;
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
        orderData.put("paymentIntentId", orderRequest.paymentIntentId());
        orderData.put("status", "Pending");

        var result = db.collection("orders").add(orderData).get();
        return result.getId();
    }
}
