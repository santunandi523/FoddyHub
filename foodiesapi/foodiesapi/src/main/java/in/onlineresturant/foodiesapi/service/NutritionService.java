package in.onlineresturant.foodiesapi.service;

import in.onlineresturant.foodiesapi.entity.FoodEntity;
import in.onlineresturant.foodiesapi.entity.NutritionInfo;
import in.onlineresturant.foodiesapi.entity.NutritionSummary;
import in.onlineresturant.foodiesapi.entity.ProposedOrderItem;
import in.onlineresturant.foodiesapi.io.ai.NutritionSummaryResponse;

import java.util.List;
import java.util.Map;

public interface NutritionService {
    NutritionSummary calculateSummaryFromProposedItems(List<ProposedOrderItem> items);
    NutritionSummary calculateSummary(Map<FoodEntity, Integer> itemsWithQuantities);
    NutritionSummaryResponse convertToResponse(NutritionSummary summary);
    NutritionInfo getNutritionForFood(String foodId);
    List<FoodEntity> filterByNutritionConstraints(List<FoodEntity> foods, Double maxCalories, Double minProtein);
}
