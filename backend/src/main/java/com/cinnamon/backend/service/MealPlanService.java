package com.cinnamon.backend.service;

import com.cinnamon.backend.model.MealPlanRequest;
import com.cinnamon.backend.model.MenuItem;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

    public String seedWeeklyMealPlans() throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        com.google.cloud.firestore.WriteBatch batch = db.batch();
        // Load menu item IDs to reference in lists
        ApiFuture<QuerySnapshot> menuFuture = db.collection("menuItems").get();
        List<QueryDocumentSnapshot> menuDocs = menuFuture.get().getDocuments();
        if (menuDocs.size() < 12) {
            return "Need at least 12 menuItems in Firestore to seed mealPlans";
        }
        List<String> ids = menuDocs.stream().map(DocumentSnapshot::getId).collect(Collectors.toList());
        List<String> vegList = ids.subList(0, 6);
        List<String> nonVegList = ids.subList(6, 12);

        List<String> days = Arrays.asList("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday");
        int index = 1;
        for (String day : days) {
            // veg
            Map<String, Object> veg = new HashMap<>();
            veg.put("day", day);
            veg.put("veg", true);
            veg.put("price", 32);
            veg.put("list", new ArrayList<>(vegList));
            batch.set(db.collection("mealPlans").document(String.valueOf(index++)), veg);

            // non-veg
            Map<String, Object> nonVeg = new HashMap<>();
            nonVeg.put("day", day);
            nonVeg.put("veg", false);
            nonVeg.put("price", 38);
            nonVeg.put("list", new ArrayList<>(nonVegList));
            batch.set(db.collection("mealPlans").document(String.valueOf(index++)), nonVeg);
        }
        batch.commit().get();
        return "Weekly meal plans have been seeded successfully.";
    }

    public String seedMenuItems() throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        com.google.cloud.firestore.WriteBatch batch = db.batch();
        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("1", "Samosa", 5, "Flaky pastry with spiced potato filling", "https://images.unsplash.com/photo-1601050690183-3b57a9c0f8b2?q=80&w=600&auto=format&fit=crop", "Appetizer", "menu"));
        menuItems.add(new MenuItem("2", "Paneer Tikka", 12, "Marinated paneer grilled to perfection", "https://images.unsplash.com/photo-1630409352402-9ad6c6c158b2?q=80&w=600&auto=format&fit=crop", "Paneer", "menu"));
        menuItems.add(new MenuItem("3", "Tomato Soup", 7, "Rich and creamy tomato soup", "https://images.unsplash.com/photo-1551218808-94e220e084d2?q=80&w=600&auto=format&fit=crop", "Soup", "menu"));
        menuItems.add(new MenuItem("4", "Manchow Soup", 8, "Spicy Indo-Chinese soup with veggies", "https://images.unsplash.com/photo-1547592180-61a069d79558?q=80&w=600&auto=format&fit=crop", "Soup", "menu"));
        menuItems.add(new MenuItem("5", "Chili Paneer", 13, "Crispy paneer tossed in chili sauce", "https://images.unsplash.com/photo-1625944619794-541f6274955f?q=80&w=600&auto=format&fit=crop", "Paneer", "menu"));
        menuItems.add(new MenuItem("6", "Aloo Tikki", 6, "Crispy potato patties with chutney", "https://images.unsplash.com/photo-1589308078054-8322fb1a3c7a?q=80&w=600&auto=format&fit=crop", "Appetizer", "menu"));
        menuItems.add(new MenuItem("7", "Hara Bhara Kebab", 9, "Spinach and peas kebab", "https://images.unsplash.com/photo-1617096012907-016d9af70906?q=80&w=600&auto=format&fit=crop", "Appetizer", "menu"));
        menuItems.add(new MenuItem("8", "Paneer Butter Masala", 14, "Paneer in rich tomato-butter gravy", "https://images.unsplash.com/photo-1642214213119-18aa04064db6?q=80&w=600&auto=format&fit=crop", "Paneer", "menu"));
        menuItems.add(new MenuItem("9", "Dal Makhani", 11, "Creamy black lentils", "https://images.unsplash.com/photo-1599566150163-29194dcaad36?q=80&w=600&auto=format&fit=crop", "Main Course", "menu"));
        menuItems.add(new MenuItem("10", "Chole Bhature", 10, "Spicy chickpeas with fried bread", "https://images.unsplash.com/photo-1604382354936-07c5d9983d34?q=80&w=600&auto=format&fit=crop", "Main Course", "menu"));
        menuItems.add(new MenuItem("11", "Malai Kofta", 15, "Paneer balls in creamy gravy", "https://images.unsplash.com/photo-1582571524779-7a0a733b44f7?q=80&w=600&auto=format&fit=crop", "Main Course", "menu"));
        menuItems.add(new MenuItem("12", "Veg Biryani", 13, "Aromatic rice with mixed vegetables", "https://images.unsplash.com/photo-1589302168068-964664d93dc0?q=80&w=600&auto=format&fit=crop", "Main Course", "menu"));
        menuItems.add(new MenuItem("13", "Masala Dosa", 9, "Crispy crepe with potato filling", "https://images.unsplash.com/photo-1626202249999-9d4a27a31f5c?q=80&w=600&auto=format&fit=crop", "South Indian", "menu"));
        menuItems.add(new MenuItem("14", "Idli Sambar", 8, "Steamed rice cakes with lentil soup", "https://images.unsplash.com/photo-1628592429842-5e4b5a2e3a4b?q=80&w=600&auto=format&fit=crop", "South Indian", "menu"));
        menuItems.add(new MenuItem("15", "Naan", 3, "Leavened oven-baked flatbread", "https://images.unsplash.com/photo-1595379735998-9873f8a43a4f?q=80&w=600&auto=format&fit=crop", "Breads", "menu"));
        menuItems.add(new MenuItem("16", "Garlic Naan", 4, "Naan with garlic and herbs", "https://images.unsplash.com/photo-1628209583133-09641d8521a8?q=80&w=600&auto=format&fit=crop", "Breads", "menu"));
        menuItems.add(new MenuItem("17", "Gulab Jamun", 5, "Deep-fried milk solids in syrup", "https://images.unsplash.com/photo-1613448362029-7987931b8a45?q=80&w=600&auto=format&fit=crop", "Dessert", "menu"));
        menuItems.add(new MenuItem("18", "Butter Chicken", 16, "Chicken in a mildly spiced tomato sauce", "https://images.unsplash.com/photo-1565299712540-6bb3e22b5f7a?q=80&w=600&auto=format&fit=crop", "Non-Veg", "menu"));
        menuItems.add(new MenuItem("19", "Chicken Tikka Masala", 17, "Grilled chicken chunks in a spiced curry", "https://images.unsplash.com/photo-1598515214211-89d3c7373b91?q=80&w=600&auto=format&fit=crop", "Non-Veg", "menu"));
        menuItems.add(new MenuItem("20", "Mutton Rogan Josh", 18, "Aromatic lamb dish", "https://images.unsplash.com/photo-1625398407132-196c11368348?q=80&w=600&auto=format&fit=crop", "Non-Veg", "menu"));

        for (MenuItem item : menuItems) {
            batch.set(db.collection("menuItems").document(item.getId()), item);
        }
        batch.commit().get();

        return "Menu items have been seeded successfully.";
    }

    public Map<String, Object> getWeeklyMealPlan() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Fetch all mealPlans
        List<QueryDocumentSnapshot> planDocs = db.collection("mealPlans").get().get().getDocuments();

        // Fetch all menu items to build id->name map
        List<QueryDocumentSnapshot> menuDocs = db.collection("menuItems").get().get().getDocuments();
        Map<String, String> idToName = new HashMap<>();
        for (QueryDocumentSnapshot doc : menuDocs) {
            Object name = doc.get("name");
            if (name != null) {
                idToName.put(doc.getId(), String.valueOf(name));
            }
        }

        // Group meal plans by day
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        List<String> dayOrder = Arrays.asList("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday");
        for (String day : dayOrder) {
            result.put(day, new HashMap<>());
        }

        for (QueryDocumentSnapshot plan : planDocs) {
            String day = String.valueOf(plan.get("day"));
            Boolean veg = (Boolean) plan.get("veg");
            Number priceNum = (Number) plan.get("price");
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) plan.get("list");
            if (day == null || veg == null || priceNum == null || list == null) continue;

            List<String> names = list.stream()
                    .map(idToName::get)
                    .filter(n -> n != null)
                    .collect(Collectors.toList());

            Map<String, Object> section = new HashMap<>();
            section.put("title", veg ? "Veg Meal" : "Non-Veg Meal");
            section.put("priceLabel", String.format("$%d +tax", priceNum.intValue()));
            section.put("items", names);

            Map<String, Object> dayObj = result.getOrDefault(day, new HashMap<>());
            dayObj.put(veg ? "veg" : "nonVeg", section);
            result.put(day, dayObj);
        }

        // Return as Map<String, Object> for JSON
        return new LinkedHashMap<>(result);
    }

    public String seedTestItem() throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> testData = new HashMap<>();
        testData.put("message", "Hello Firestore");
        testData.put("timestamp", System.currentTimeMillis());
        db.collection("test").document("connectivity_check").set(testData).get();
        return "Connectivity check passed.";
    }
}
