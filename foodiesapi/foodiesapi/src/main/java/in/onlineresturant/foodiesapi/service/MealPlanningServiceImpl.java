package in.onlineresturant.foodiesapi.service;

import in.onlineresturant.foodiesapi.entity.FoodEntity;
import in.onlineresturant.foodiesapi.entity.ProposedOrderItem;
import in.onlineresturant.foodiesapi.io.ai.FoodSearchCriteria;
import in.onlineresturant.foodiesapi.io.ai.MealPlanRequest;
import in.onlineresturant.foodiesapi.io.ai.ProposedOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealPlanningServiceImpl implements MealPlanningService {

    private final FoodSearchService foodSearchService;
    private final ProposedOrderService proposedOrderService;

    @Override
    public ProposedOrderResponse buildMealPlan(MealPlanRequest request, String userId, String sessionId) {
        int people = request.getNumberOfPeople() != null && request.getNumberOfPeople() > 0 ? request.getNumberOfPeople() : 1;
        Double budget = request.getBudget() != null && request.getBudget() > 0 ? request.getBudget() : 1000.0;

        // Account for standard ₹10 delivery + 10% tax when matching budget
        // Target subtotal = (budget - 10) / 1.10
        double targetSubtotal = (budget > 20) ? (budget - 10.0) / 1.10 : budget;

        FoodSearchCriteria criteria = FoodSearchCriteria.builder()
                .isVegetarian(request.getIsVegetarian())
                .spicePreference(request.getSpicePreference())
                .maxCalories(request.getMaxCalories())
                .minProtein(request.getMinProtein())
                .availableOnly(true)
                .maxPrice(targetSubtotal)
                .limit(30)
                .build();

        List<FoodEntity> candidates = foodSearchService.searchFoods(criteria);

        List<ProposedOrderItem> selectedItems = new ArrayList<>();
        double currentSubtotal = 0.0;

        if (!candidates.isEmpty()) {
            if (people == 1) {
                // Select best matching main dish
                FoodEntity best = candidates.get(0);
                selectedItems.add(createItem(best, 1));
            } else if (people == 2) {
                // For 2 people: choose 2 dishes or 1 high portion if budget allows
                FoodEntity first = candidates.get(0);
                selectedItems.add(createItem(first, 1));
                currentSubtotal += first.getPrice();

                // Find a second distinct dish that fits within remaining subtotal budget
                double remaining = targetSubtotal - currentSubtotal;
                FoodEntity second = candidates.stream()
                        .filter(f -> !f.getId().equals(first.getId()) && f.getPrice() <= remaining)
                        .findFirst()
                        .orElse(null);

                if (second != null) {
                    selectedItems.add(createItem(second, 1));
                } else if (remaining >= first.getPrice()) {
                    // Or 2 portions of the first dish
                    selectedItems.get(0).setQuantity(2);
                }
            } else {
                // For 3+ people: allocate portions
                int remainingPortions = people;
                for (FoodEntity food : candidates) {
                    if (remainingPortions <= 0) break;
                    if (currentSubtotal + food.getPrice() <= targetSubtotal) {
                        selectedItems.add(createItem(food, 1));
                        currentSubtotal += food.getPrice();
                        remainingPortions--;
                    }
                }
                // If budget allows and still need portions, duplicate first
                if (remainingPortions > 0 && !selectedItems.isEmpty()) {
                    ProposedOrderItem top = selectedItems.get(0);
                    while (remainingPortions > 0 && currentSubtotal + top.getUnitPrice() <= targetSubtotal) {
                        top.setQuantity(top.getQuantity() + 1);
                        currentSubtotal += top.getUnitPrice();
                        remainingPortions--;
                    }
                }
            }
        }

        return proposedOrderService.setProposedOrderItems(userId, sessionId, selectedItems);
    }

    private ProposedOrderItem createItem(FoodEntity food, int qty) {
        return ProposedOrderItem.builder()
                .foodId(food.getId())
                .name(food.getName())
                .category(food.getCategory())
                .imageUrl(food.getImageUrl())
                .unitPrice(food.getPrice())
                .quantity(qty)
                .itemSubtotal(food.getPrice() * qty)
                .nutrition(food.getNutrition())
                .isVegetarian(food.getIsVegetarian())
                .spiceLevel(food.getSpiceLevel())
                .build();
    }
}
