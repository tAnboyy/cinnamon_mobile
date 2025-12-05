package com.cinnamon.backend.model;

import java.util.List;
import java.util.Map;

public record OrderRequest(
    List<CartItem> items,
    String userId,
    String contactNumber,
    String notes,
    String paymentMethod, // "online" | "cash"
    String paymentIntentId,
    double totalAmount,
    String pickupDate, // YYYY-MM-DD
    String pickupTime // HH:MM
) {}
