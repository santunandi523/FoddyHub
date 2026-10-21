package in.onlineresturant.foodiesapi.service;

import in.onlineresturant.foodiesapi.entity.FoodEntity;
import in.onlineresturant.foodiesapi.io.FoodResponse;
import in.onlineresturant.foodiesapi.io.ai.FoodSearchCriteria;

import java.util.List;

public interface FoodSearchService {
    List<FoodEntity> searchFoods(FoodSearchCriteria criteria);
    List<FoodResponse> searchFoodResponses(FoodSearchCriteria criteria);
    FoodEntity findFoodByIdOrName(String identifier);
    FoodResponse convertToResponse(FoodEntity entity);
}
