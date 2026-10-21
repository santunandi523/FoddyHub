package in.onlineresturant.foodiesapi.service;

import in.onlineresturant.foodiesapi.entity.FoodEntity;
import in.onlineresturant.foodiesapi.entity.NutritionInfo;
import in.onlineresturant.foodiesapi.io.FoodResponse;
import in.onlineresturant.foodiesapi.io.ai.FoodSearchCriteria;
import in.onlineresturant.foodiesapi.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodSearchServiceImpl implements FoodSearchService {

    private final FoodRepository foodRepository;
    private final NutritionService nutritionService;

    @Override
    public List<FoodEntity> searchFoods(FoodSearchCriteria criteria) {
        List<FoodEntity> allFoods = foodRepository.findAll();

        return allFoods.stream()
                .filter(food -> {
                    // Availability check
                    boolean reqAvailableOnly = criteria.getAvailableOnly() == null || criteria.getAvailableOnly();
                    if (reqAvailableOnly && Boolean.FALSE.equals(food.getAvailable())) {
                        return false;
                    }

                    // Category check
                    if (StringUtils.hasText(criteria.getCategory())) {
                        String reqCat = criteria.getCategory().trim().toLowerCase();
                        if (food.getCategory() == null || !food.getCategory().toLowerCase().contains(reqCat)) {
                            return false;
                        }
                    }

                    // Budget / Price check
                    if (criteria.getMaxPrice() != null && criteria.getMaxPrice() > 0) {
                        if (food.getPrice() > criteria.getMaxPrice()) {
                            return false;
                        }
                    }

                    // Vegetarian / Non-vegetarian preference
                    if (criteria.getIsVegetarian() != null) {
                        Boolean foodVeg = isFoodVegetarian(food);
                        if (foodVeg != null && !foodVeg.equals(criteria.getIsVegetarian())) {
                            return false;
                        }
                    }

                    // Spice level preference
                    if (StringUtils.hasText(criteria.getSpicePreference())) {
                        String pref = criteria.getSpicePreference().trim().toLowerCase();
                        String level = food.getSpiceLevel() != null ? food.getSpiceLevel().toLowerCase() : "";
                        String desc = food.getDescription() != null ? food.getDescription().toLowerCase() : "";
                        String name = food.getName() != null ? food.getName().toLowerCase() : "";
                        boolean matchesSpice = level.contains(pref) || desc.contains(pref) || name.contains(pref);
                        if (!matchesSpice) {
                            return false;
                        }
                    }

                    // Calorie constraint
                    if (criteria.getMaxCalories() != null && criteria.getMaxCalories() > 0) {
                        NutritionInfo info = food.getNutrition();
                        if (info != null && info.getCalories() != null && info.getCalories() > criteria.getMaxCalories()) {
                            return false;
                        }
                    }

                    // Protein constraint
                    if (criteria.getMinProtein() != null && criteria.getMinProtein() > 0) {
                        NutritionInfo info = food.getNutrition();
                        if (info != null && info.getProtein() != null && info.getProtein() < criteria.getMinProtein()) {
                            return false;
                        }
                    }

                    // General keyword search
                    if (StringUtils.hasText(criteria.getQuery())) {
                        String q = criteria.getQuery().trim().toLowerCase();
                        boolean nameMatch = food.getName() != null && food.getName().toLowerCase().contains(q);
                        boolean descMatch = food.getDescription() != null && food.getDescription().toLowerCase().contains(q);
                        boolean catMatch = food.getCategory() != null && food.getCategory().toLowerCase().contains(q);
                        if (!nameMatch && !descMatch && !catMatch) {
                            return false;
                        }
                    }

                    return true;
                })
                .sorted((f1, f2) -> {
                    // Relevance ranking: items matching query directly in name first, then by lower price
                    if (StringUtils.hasText(criteria.getQuery())) {
                        String q = criteria.getQuery().trim().toLowerCase();
                        boolean f1Name = f1.getName() != null && f1.getName().toLowerCase().contains(q);
                        boolean f2Name = f2.getName() != null && f2.getName().toLowerCase().contains(q);
                        if (f1Name && !f2Name) return -1;
                        if (!f1Name && f2Name) return 1;
                    }
                    return Double.compare(f1.getPrice(), f2.getPrice());
                })
                .limit(criteria.getLimit() != null && criteria.getLimit() > 0 ? criteria.getLimit() : 20)
                .collect(Collectors.toList());
    }

    @Override
    public List<FoodResponse> searchFoodResponses(FoodSearchCriteria criteria) {
        return searchFoods(criteria).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public FoodEntity findFoodByIdOrName(String identifier) {
        if (!StringUtils.hasText(identifier)) return null;

        String idf = identifier.trim();
        // 1. Exact ID
        Optional<FoodEntity> byId = foodRepository.findById(idf);
        if (byId.isPresent()) return byId.get();

        List<FoodEntity> all = foodRepository.findAll();
        // 2. Exact Name (case-insensitive)
        for (FoodEntity f : all) {
            if (f.getName() != null && f.getName().equalsIgnoreCase(idf)) {
                return f;
            }
        }

        // 3. Name contains
        String lower = idf.toLowerCase();
        for (FoodEntity f : all) {
            if (f.getName() != null && f.getName().toLowerCase().contains(lower)) {
                return f;
            }
        }

        // 4. Word overlap
        String[] words = lower.split("\\s+");
        for (FoodEntity f : all) {
            if (f.getName() != null) {
                String fName = f.getName().toLowerCase();
                for (String w : words) {
                    if (w.length() > 3 && fName.contains(w)) {
                        return f;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public FoodResponse convertToResponse(FoodEntity entity) {
        if (entity == null) return null;
        return FoodResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl())
                .available(entity.getAvailable() != null ? entity.getAvailable() : true)
                .isVegetarian(isFoodVegetarian(entity))
                .spiceLevel(entity.getSpiceLevel())
                .nutrition(entity.getNutrition())
                .build();
    }

    private Boolean isFoodVegetarian(FoodEntity food) {
        if (food.getIsVegetarian() != null) return food.getIsVegetarian();
        if (food.getNutrition() != null && food.getNutrition().getIsVegetarian() != null) {
            return food.getNutrition().getIsVegetarian();
        }
        String name = food.getName() != null ? food.getName().toLowerCase() : "";
        String cat = food.getCategory() != null ? food.getCategory().toLowerCase() : "";
        if (name.contains("chicken") || name.contains("mutton") || name.contains("fish") || name.contains("egg") || name.contains("prawn")) {
            return false;
        }
        if (name.contains("paneer") || name.contains("veg") || name.contains("dal") || cat.contains("salad") || cat.contains("cake") || cat.contains("icecreame")) {
            return true;
        }
        return null;
    }
}
