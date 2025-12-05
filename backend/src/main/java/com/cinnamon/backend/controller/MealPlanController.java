package com.cinnamon.backend.controller;

import com.cinnamon.backend.model.MealPlanRequest;
import com.cinnamon.backend.service.MealPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(" ")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody MealPlanRequest mealPlanRequest) throws ExecutionException, InterruptedException {
        String planId = mealPlanService.createMealPlan(mealPlanRequest);
        return ResponseEntity.ok("Meal plan created with ID: " + planId);
    }

    @PostMapping("/scheduler")
    public ResponseEntity<String> scheduler() {
        // This would be triggered by a cron job to generate daily orders
        return ResponseEntity.ok("scheduler stub");
    }

    @GetMapping("/weekly")
    public ResponseEntity<Object> weekly() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(mealPlanService.getWeeklyMealPlan());
    }
}
