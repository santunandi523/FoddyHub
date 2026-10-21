package in.onlineresturant.foodiesapi.io.ai;

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
public class NutritionSummaryResponse {
    private Double totalCalories;
    private Double totalProtein;
    private Double totalCarbohydrates;
    private Double totalFat;
    private Double totalFiber;
    private Double totalSugar;
    private Boolean isCompleteData;
    @Builder.Default
    private List<String> missingNutritionDishes = new ArrayList<>();
    private Boolean isVerified;
    private String note;
}
