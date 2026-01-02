package com.cinnamon.backend.service;

import com.cinnamon.backend.model.OrderRequest;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

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

    public List<Map<String, Object>> getUserOrders(String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        long t0 = System.currentTimeMillis();
    QuerySnapshot snapshot = db.collection("orders")
        .whereEqualTo("userId", userId)
        // Avoid composite index requirement; we'll sort in-memory by createdAt
        .get()
        .get();
        long t1 = System.currentTimeMillis();
        log.info("[OrderService] getUserOrders query userId={} took {} ms docs={}", userId, (t1 - t0), snapshot.size());

        List<Map<String, Object>> orders = snapshot.getDocuments().stream().map(doc -> {
            Map<String, Object> data = doc.getData();
            data.put("id", doc.getId());
            return data;
        }).toList();

        // Copy to mutable list before sorting to avoid UnsupportedOperationException
        orders = new java.util.ArrayList<>(orders);

        // Sort by createdAt ascending if present
        orders.sort((a, b) -> {
            Object ta = a.get("createdAt");
            Object tb = b.get("createdAt");
            if (ta == null && tb == null) return 0;
            if (ta == null) return -1;
            if (tb == null) return 1;
            // Firestore returns com.google.cloud.Timestamp; compare via toString or micros
            try {
                com.google.cloud.Timestamp tsa = (com.google.cloud.Timestamp) ta;
                com.google.cloud.Timestamp tsb = (com.google.cloud.Timestamp) tb;
                return tsa.compareTo(tsb);
            } catch (ClassCastException e) {
                return ta.toString().compareTo(tb.toString());
            }
        });
        log.info("[OrderService] getUserOrders returning {} orders after sort", orders.size());

        return orders;
    }

    public List<Map<String, Object>> getOrdersForMonth(int year, int month) throws ExecutionException, InterruptedException {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }

        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate nextMonth = firstDay.plusMonths(1);
        Instant startInstant = firstDay.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endInstant = nextMonth.atStartOfDay().toInstant(ZoneOffset.UTC);

        Timestamp startTs = Timestamp.ofTimeSecondsAndNanos(startInstant.getEpochSecond(), startInstant.getNano());
        Timestamp endTs = Timestamp.ofTimeSecondsAndNanos(endInstant.getEpochSecond(), endInstant.getNano());

        Firestore db = FirestoreClient.getFirestore();
        long t0 = System.currentTimeMillis();
    QuerySnapshot snapshot = db.collection("orders")
        .whereGreaterThanOrEqualTo("createdAt", Objects.requireNonNull(startTs))
        .whereLessThan("createdAt", Objects.requireNonNull(endTs))
                .get()
                .get();
        long t1 = System.currentTimeMillis();
        log.info("[OrderService] getOrdersForMonth year={} month={} took {} ms docs={}", year, month, (t1 - t0), snapshot.size());

        List<Map<String, Object>> orders = new ArrayList<>(snapshot.getDocuments().stream().map(doc -> {
            Map<String, Object> data = doc.getData();
            data.put("id", doc.getId());
            return data;
        }).toList());

        orders.sort((a, b) -> {
            Object ta = a.get("createdAt");
            Object tb = b.get("createdAt");
            if (ta == null && tb == null) return 0;
            if (ta == null) return -1;
            if (tb == null) return 1;
            try {
                Timestamp tsa = (Timestamp) ta;
                Timestamp tsb = (Timestamp) tb;
                return tsa.compareTo(tsb);
            } catch (ClassCastException e) {
                return ta.toString().compareTo(tb.toString());
            }
        });

        return orders;
    }
}
