package in.onlineresturant.foodiesapi.io.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodSearchCriteria {
    private String query;
    private String category;
    private Double maxPrice;
    private Boolean isVegetarian;
    private String spicePreference; // "mild", "medium", "spicy"
    private Double minProtein;
    private Double maxCalories;
    private Boolean availableOnly;
    private Integer limit;
}
