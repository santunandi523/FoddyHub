package in.onlineresturant.foodiesapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionInfo {
    private Double calories; // in kcal
    private Double protein; // in grams
    private Double carbohydrates; // in grams
    private Double fat; // in grams
    private Double fiber; // in grams
    private Double sugar; // in grams
    private Double servingSize;
    private String servingUnit; // "g", "ml", "portion", "piece", "plate"
    private Boolean isVegetarian;
    private List<String> allergens;
    private String dataSource; // e.g. "Restaurant Standard", "USDA Verified", "Chef Estimate"
    private Boolean isVerified; // true = lab/restaurant verified, false = estimated
}
