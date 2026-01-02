package com.cinnamon.backend.controller;

import com.cinnamon.backend.service.MealPlanService;
import com.cinnamon.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MealPlanService mealPlanService;
    private final OrderService orderService;

    public AdminController(MealPlanService mealPlanService, OrderService orderService) {
        this.mealPlanService = mealPlanService;
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Map<String, Object>>> getOrdersForMonth(
            @RequestParam int year,
            @RequestParam int month) throws ExecutionException, InterruptedException {
        List<Map<String, Object>> orders = orderService.getOrdersForMonth(year, month);
        return ResponseEntity.ok(orders);
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
