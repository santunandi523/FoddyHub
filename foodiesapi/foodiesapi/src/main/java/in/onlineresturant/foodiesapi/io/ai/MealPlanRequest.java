package in.onlineresturant.foodiesapi.io.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanRequest {
    private Double budget;
    private Integer numberOfPeople;
    private Boolean isVegetarian;
    private String spicePreference;
    private Double maxCalories;
    private Double minProtein;
}
