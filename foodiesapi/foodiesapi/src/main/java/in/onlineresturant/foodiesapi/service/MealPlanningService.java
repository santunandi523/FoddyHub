package in.onlineresturant.foodiesapi.service;

import in.onlineresturant.foodiesapi.io.ai.MealPlanRequest;
import in.onlineresturant.foodiesapi.io.ai.ProposedOrderResponse;

public interface MealPlanningService {
    ProposedOrderResponse buildMealPlan(MealPlanRequest request, String userId, String sessionId);
}
