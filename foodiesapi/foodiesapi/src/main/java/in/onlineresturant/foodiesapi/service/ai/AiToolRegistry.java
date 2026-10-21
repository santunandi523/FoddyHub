package in.onlineresturant.foodiesapi.service.ai;

import in.onlineresturant.foodiesapi.entity.FoodEntity;
import in.onlineresturant.foodiesapi.entity.NutritionInfo;
import in.onlineresturant.foodiesapi.io.FoodResponse;
import in.onlineresturant.foodiesapi.io.ai.*;
import in.onlineresturant.foodiesapi.service.FoodSearchService;
import in.onlineresturant.foodiesapi.service.MealPlanningService;
import in.onlineresturant.foodiesapi.service.NutritionService;
import in.onlineresturant.foodiesapi.service.ProposedOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AiToolRegistry {

    private final FoodSearchService foodSearchService;
    private final NutritionService nutritionService;
    private final MealPlanningService mealPlanningService;
    private final ProposedOrderService proposedOrderService;

    public static final String TOOL_SEARCH_FOODS = "searchAvailableFoods";
    public static final String TOOL_GET_FOOD_DETAILS = "getFoodDetails";
    public static final String TOOL_GET_FOOD_NUTRITION = "getFoodNutrition";
    public static final String TOOL_BUILD_MEAL_PLAN = "buildMealPlan";
    public static final String TOOL_MODIFY_PROPOSED_ORDER = "modifyProposedOrder";
    public static final String TOOL_GET_PROPOSED_ORDER_SUMMARY = "getProposedOrderSummary";

    /**
     * Executes a tool with validated parameters.
     */
    public Object executeTool(String toolName, Map<String, Object> args, String userId, String sessionId) {
        if (args == null) args = Collections.emptyMap();

        switch (toolName) {
            case TOOL_SEARCH_FOODS -> {
                FoodSearchCriteria criteria = FoodSearchCriteria.builder()
                        .query(getString(args, "query"))
                        .category(getString(args, "category"))
                        .maxPrice(getDouble(args, "maxPrice"))
                        .isVegetarian(getBoolean(args, "isVegetarian"))
                        .spicePreference(getString(args, "spicePreference"))
                        .minProtein(getDouble(args, "minProtein"))
                        .maxCalories(getDouble(args, "maxCalories"))
                        .availableOnly(true)
                        .limit(getInteger(args, "limit", 10))
                        .build();
                List<FoodResponse> results = foodSearchService.searchFoodResponses(criteria);
                if (results.isEmpty()) {
                    // Provide helpful available alternatives
                    List<FoodResponse> alternatives = foodSearchService.searchFoodResponses(
                            FoodSearchCriteria.builder().availableOnly(true).limit(4).build());
                    Map<String, Object> response = new HashMap<>();
                    response.put("found", false);
                    response.put("message", "No dishes exactly matched your criteria. Here are popular available alternatives:");
                    response.put("alternatives", alternatives);
                    return response;
                }
                return results;
            }

            case TOOL_GET_FOOD_DETAILS -> {
                String identifier = getString(args, "foodId");
                if (identifier == null) identifier = getString(args, "foodName");
                if (identifier == null) identifier = getString(args, "query");

                FoodEntity food = foodSearchService.findFoodByIdOrName(identifier);
                if (food == null) {
                    Map<String, Object> notFound = new HashMap<>();
                    notFound.put("found", false);
                    notFound.put("message", "Dish not found: " + identifier);
                    return notFound;
                }
                return foodSearchService.convertToResponse(food);
            }

            case TOOL_GET_FOOD_NUTRITION -> {
                String identifier = getString(args, "foodId");
                if (identifier == null) identifier = getString(args, "foodName");
                if (identifier == null) identifier = getString(args, "query");

                FoodEntity food = foodSearchService.findFoodByIdOrName(identifier);
                if (food == null) {
                    Map<String, Object> notFound = new HashMap<>();
                    notFound.put("found", false);
                    notFound.put("message", "Dish not found: " + identifier);
                    return notFound;
                }
                NutritionInfo info = food.getNutrition();
                Map<String, Object> res = new HashMap<>();
                res.put("foodId", food.getId());
                res.put("foodName", food.getName());
                res.put("nutrition", info);
                res.put("available", food.getAvailable());
                res.put("price", food.getPrice());
                if (info == null || info.getCalories() == null) {
                    res.put("status", "Nutrition information unavailable for this dish.");
                } else {
                    res.put("isVerified", info.getIsVerified());
                    res.put("dataSource", info.getDataSource());
                }
                return res;
            }

            case TOOL_BUILD_MEAL_PLAN -> {
                MealPlanRequest req = MealPlanRequest.builder()
                        .budget(getDouble(args, "budget"))
                        .numberOfPeople(getInteger(args, "numberOfPeople", 1))
                        .isVegetarian(getBoolean(args, "isVegetarian"))
                        .spicePreference(getString(args, "spicePreference"))
                        .maxCalories(getDouble(args, "maxCalories"))
                        .minProtein(getDouble(args, "minProtein"))
                        .build();
                return mealPlanningService.buildMealPlan(req, userId, sessionId);
            }

            case TOOL_MODIFY_PROPOSED_ORDER -> {
                String actionStr = getString(args, "action");
                ModifyProposedOrderRequest.Action action = ModifyProposedOrderRequest.Action.ADD;
                if (actionStr != null) {
                    try {
                        action = ModifyProposedOrderRequest.Action.valueOf(actionStr.toUpperCase());
                    } catch (Exception ignored) {
                        if (actionStr.equalsIgnoreCase("remove")) action = ModifyProposedOrderRequest.Action.REMOVE;
                        else if (actionStr.equalsIgnoreCase("replace")) action = ModifyProposedOrderRequest.Action.REPLACE;
                        else if (actionStr.equalsIgnoreCase("clear")) action = ModifyProposedOrderRequest.Action.CLEAR;
                        else if (actionStr.equalsIgnoreCase("update")) action = ModifyProposedOrderRequest.Action.UPDATE_QTY;
                    }
                }

                ModifyProposedOrderRequest req = ModifyProposedOrderRequest.builder()
                        .action(action)
                        .foodId(getString(args, "foodId"))
                        .foodName(getString(args, "foodName"))
                        .replaceWithFoodId(getString(args, "replaceWithFoodId"))
                        .replaceWithFoodName(getString(args, "replaceWithFoodName"))
                        .quantity(getInteger(args, "quantity", 1))
                        .build();
                return proposedOrderService.modifyProposedOrder(userId, sessionId, req);
            }

            case TOOL_GET_PROPOSED_ORDER_SUMMARY -> {
                return proposedOrderService.getProposedOrder(userId, sessionId);
            }

            default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString().trim() : null;
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try {
            return Double.parseDouble(val.toString().replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getInteger(Map<String, Object> map, String key, Integer defaultVal) {
        Object val = map.get(key);
        if (val == null) return defaultVal;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return Integer.parseInt(val.toString().replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private Boolean getBoolean(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof Boolean) return (Boolean) val;
        String s = val.toString().trim().toLowerCase();
        if ("true".equals(s) || "yes".equals(s)) return true;
        if ("false".equals(s) || "no".equals(s)) return false;
        return null;
    }
}
