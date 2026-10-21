package in.onlineresturant.foodiesapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionSummary {
    private Double totalCalories;
    private Double totalProtein;
    private Double totalCarbohydrates;
    private Double totalFat;
    private Double totalFiber;
    private Double totalSugar;
    private Boolean isCompleteData; // false if some dishes lack nutrition data
    @Builder.Default
    private List<String> missingNutritionDishes = new ArrayList<>();
    private Boolean isVerified; // true if all items are verified
}
