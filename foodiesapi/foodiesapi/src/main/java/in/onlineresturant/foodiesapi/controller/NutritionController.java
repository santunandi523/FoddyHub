package in.onlineresturant.foodiesapi.controller;

import in.onlineresturant.foodiesapi.entity.FoodEntity;
import in.onlineresturant.foodiesapi.entity.NutritionInfo;
import in.onlineresturant.foodiesapi.entity.NutritionSummary;
import in.onlineresturant.foodiesapi.io.ai.NutritionSummaryResponse;
import in.onlineresturant.foodiesapi.repository.FoodRepository;
import in.onlineresturant.foodiesapi.service.NutritionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
@CrossOrigin("*")
public class NutritionController {

    private final NutritionService nutritionService;
    private final FoodRepository foodRepository;

    @GetMapping("/food/{foodId}")
    public ResponseEntity<Map<String, Object>> getFoodNutrition(@PathVariable String foodId) {
        FoodEntity food = foodRepository.findById(foodId).orElse(null);
        if (food == null) {
            return ResponseEntity.notFound().build();
        }

        NutritionInfo nutrition = food.getNutrition();
        Map<String, Object> response = new HashMap<>();
        response.put("foodId", food.getId());
        response.put("foodName", food.getName());
        response.put("category", food.getCategory());
        response.put("isVegetarian", food.getIsVegetarian());
        response.put("spiceLevel", food.getSpiceLevel());
        response.put("nutrition", nutrition);

        if (nutrition == null || nutrition.getCalories() == null) {
            response.put("status", "Nutrition information unavailable");
        } else {
            response.put("isVerified", nutrition.getIsVerified());
            response.put("dataSource", nutrition.getDataSource());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/calculate")
    public ResponseEntity<NutritionSummaryResponse> calculateNutrition(@RequestBody Map<String, Integer> foodIdQuantities) {
        Map<FoodEntity, Integer> items = new HashMap<>();
        if (foodIdQuantities != null) {
            for (Map.Entry<String, Integer> entry : foodIdQuantities.entrySet()) {
                if (entry.getValue() != null && entry.getValue() > 0) {
                    foodRepository.findById(entry.getKey()).ifPresent(food -> items.put(food, entry.getValue()));
                }
            }
        }

        NutritionSummary summary = nutritionService.calculateSummary(items);
        return ResponseEntity.ok(nutritionService.convertToResponse(summary));
    }
}
