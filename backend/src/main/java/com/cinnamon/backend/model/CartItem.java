package com.cinnamon.backend.model;

import java.util.List;

public record CartItem(
    String id,
    String name,
    int quantity,
    double price
) {}
