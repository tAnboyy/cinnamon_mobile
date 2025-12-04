package com.cinnamon.backend.model;

import java.util.List;

public record MealPlanRequest(
    String userId,
    String startDate,
    String endDate,
    List<String> daysOfWeek,
    String pickupTime,
    List<CartItem> items
) {}
