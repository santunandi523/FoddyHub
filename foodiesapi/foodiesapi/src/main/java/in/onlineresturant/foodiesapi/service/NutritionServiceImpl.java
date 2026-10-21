package in.onlineresturant.foodiesapi.service;

import in.onlineresturant.foodiesapi.entity.FoodEntity;
import in.onlineresturant.foodiesapi.entity.NutritionInfo;
import in.onlineresturant.foodiesapi.entity.NutritionSummary;
import in.onlineresturant.foodiesapi.entity.ProposedOrderItem;
import in.onlineresturant.foodiesapi.io.ai.NutritionSummaryResponse;
import in.onlineresturant.foodiesapi.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NutritionServiceImpl implements NutritionService {

    private final FoodRepository foodRepository;

    @Override
    public NutritionSummary calculateSummaryFromProposedItems(List<ProposedOrderItem> items) {
        if (items == null || items.isEmpty()) {
            return NutritionSummary.builder()
                    .totalCalories(0.0)
                    .totalProtein(0.0)
                    .totalCarbohydrates(0.0)
                    .totalFat(0.0)
                    .totalFiber(0.0)
                    .totalSugar(0.0)
                    .isCompleteData(true)
                    .missingNutritionDishes(new ArrayList<>())
                    .isVerified(true)
                    .build();
        }

        Map<FoodEntity, Integer> map = new HashMap<>();
        for (ProposedOrderItem item : items) {
            if (item.getFoodId() != null) {
                FoodEntity food = foodRepository.findById(item.getFoodId()).orElse(null);
                if (food != null) {
                    map.put(food, item.getQuantity());
                } else if (item.getNutrition() != null) {
                    // Fallback to item's embedded nutrition if entity not directly resolved
                    FoodEntity mockEntity = FoodEntity.builder()
                            .id(item.getFoodId())
                            .name(item.getName())
                            .nutrition(item.getNutrition())
                            .build();
                    map.put(mockEntity, item.getQuantity());
                } else {
                    FoodEntity missingEntity = FoodEntity.builder()
                            .id(item.getFoodId())
                            .name(item.getName() != null ? item.getName() : "Item " + item.getFoodId())
                            .build();
                    map.put(missingEntity, item.getQuantity());
                }
            }
        }
        return calculateSummary(map);
    }

    @Override
    public NutritionSummary calculateSummary(Map<FoodEntity, Integer> itemsWithQuantities) {
        double calories = 0.0;
        double protein = 0.0;
        double carbs = 0.0;
        double fat = 0.0;
        double fiber = 0.0;
        double sugar = 0.0;

        boolean isComplete = true;
        boolean allVerified = true;
        List<String> missingDishes = new ArrayList<>();

        if (itemsWithQuantities != null) {
            for (Map.Entry<FoodEntity, Integer> entry : itemsWithQuantities.entrySet()) {
                FoodEntity food = entry.getKey();
                int qty = entry.getValue() != null ? entry.getValue() : 1;
                if (qty <= 0) continue;

                NutritionInfo info = food.getNutrition();
                if (info == null || info.getCalories() == null) {
                    isComplete = false;
                    missingDishes.add(food.getName() != null ? food.getName() : "Unknown Dish");
                    continue;
                }

                if (info.getIsVerified() == null || !info.getIsVerified()) {
                    allVerified = false;
                }

                calories += (info.getCalories() != null ? info.getCalories() : 0.0) * qty;
                protein += (info.getProtein() != null ? info.getProtein() : 0.0) * qty;
                carbs += (info.getCarbohydrates() != null ? info.getCarbohydrates() : 0.0) * qty;
                fat += (info.getFat() != null ? info.getFat() : 0.0) * qty;
                fiber += (info.getFiber() != null ? info.getFiber() : 0.0) * qty;
                sugar += (info.getSugar() != null ? info.getSugar() : 0.0) * qty;
            }
        }

        return NutritionSummary.builder()
                .totalCalories(round(calories))
                .totalProtein(round(protein))
                .totalCarbohydrates(round(carbs))
                .totalFat(round(fat))
                .totalFiber(round(fiber))
                .totalSugar(round(sugar))
                .isCompleteData(isComplete)
                .missingNutritionDishes(missingDishes)
                .isVerified(allVerified && isComplete)
                .build();
    }

    @Override
    public NutritionSummaryResponse convertToResponse(NutritionSummary summary) {
        if (summary == null) return null;

        String note = null;
        if (!summary.getIsCompleteData()) {
            note = "Nutrition information unavailable for: " + String.join(", ", summary.getMissingNutritionDishes())
                    + ". Totals represent only dishes with verified/estimated data.";
        } else if (!Boolean.TRUE.equals(summary.getIsVerified())) {
            note = "Some or all nutritional metrics are estimated from standard recipe guidelines.";
        }

        return NutritionSummaryResponse.builder()
                .totalCalories(summary.getTotalCalories())
                .totalProtein(summary.getTotalProtein())
                .totalCarbohydrates(summary.getTotalCarbohydrates())
                .totalFat(summary.getTotalFat())
                .totalFiber(summary.getTotalFiber())
                .totalSugar(summary.getTotalSugar())
                .isCompleteData(summary.getIsCompleteData())
                .missingNutritionDishes(summary.getMissingNutritionDishes())
                .isVerified(summary.getIsVerified())
                .note(note)
                .build();
    }

    @Override
    public NutritionInfo getNutritionForFood(String foodId) {
        return foodRepository.findById(foodId)
                .map(FoodEntity::getNutrition)
                .orElse(null);
    }

    @Override
    public List<FoodEntity> filterByNutritionConstraints(List<FoodEntity> foods, Double maxCalories, Double minProtein) {
        if (foods == null) return new ArrayList<>();

        return foods.stream()
                .filter(food -> {
                    NutritionInfo info = food.getNutrition();
                    if (info == null) return false;
                    if (maxCalories != null && info.getCalories() != null && info.getCalories() > maxCalories) {
                        return false;
                    }
                    if (minProtein != null && info.getProtein() != null && info.getProtein() < minProtein) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
