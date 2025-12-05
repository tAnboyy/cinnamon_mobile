package com.cinnamon.backend.controller;

import com.cinnamon.backend.service.MealPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MealPlanService mealPlanService;

    public AdminController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }
    @GetMapping("/orders/live")
    public ResponseEntity<String> liveOrders() {
        return ResponseEntity.ok("live orders stub");
    }

    @PostMapping("/orders/status")
    public ResponseEntity<String> updateStatus() {
        return ResponseEntity.ok("update status stub");
    }

    @PostMapping("/seed/meal-plans")
    public ResponseEntity<String> seedMealPlans() throws Exception {
        String result = mealPlanService.seedWeeklyMealPlans();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/seed/menu-items")
    public ResponseEntity<String> seedMenuItems() throws Exception {
        String result = mealPlanService.seedMenuItems();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/seed/test")
    public ResponseEntity<String> seedTest() throws Exception {
        String result = mealPlanService.seedTestItem();
        return ResponseEntity.ok(result);
    }
}
