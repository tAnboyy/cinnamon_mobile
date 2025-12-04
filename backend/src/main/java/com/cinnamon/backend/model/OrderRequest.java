package com.cinnamon.backend.model;

import java.util.List;

public record OrderRequest(
    List<CartItem> items,
    String userId,
    String paymentIntentId
) {}
