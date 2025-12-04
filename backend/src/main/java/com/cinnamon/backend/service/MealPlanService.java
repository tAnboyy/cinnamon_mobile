package com.cinnamon.backend.service;

import com.cinnamon.backend.model.MealPlanRequest;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class MealPlanService {

    public String createMealPlan(MealPlanRequest mealPlanRequest) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> mealPlanData = new HashMap<>();
        mealPlanData.put("userId", mealPlanRequest.userId());
        mealPlanData.put("startDate", mealPlanRequest.startDate());
        mealPlanData.put("endDate", mealPlanRequest.endDate());
        mealPlanData.put("daysOfWeek", mealPlanRequest.daysOfWeek());
        mealPlanData.put("pickupTime", mealPlanRequest.pickupTime());
        mealPlanData.put("items", mealPlanRequest.items());
        mealPlanData.put("active", true);

        var result = db.collection("mealPlans").add(mealPlanData).get();
        return result.getId();
    }
}
