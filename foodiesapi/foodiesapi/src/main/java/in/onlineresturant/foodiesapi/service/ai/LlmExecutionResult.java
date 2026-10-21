package in.onlineresturant.foodiesapi.service.ai;

import in.onlineresturant.foodiesapi.io.FoodResponse;
import in.onlineresturant.foodiesapi.io.ai.NutritionSummaryResponse;
import in.onlineresturant.foodiesapi.io.ai.ProposedOrderResponse;
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
public class LlmExecutionResult {
    private String responseText;
    @Builder.Default
    private List<FoodResponse> foodRecommendations = new ArrayList<>();
    private ProposedOrderResponse proposedOrder;
    private NutritionSummaryResponse nutritionSummary;
    @Builder.Default
    private List<String> suggestedActions = new ArrayList<>();
}
