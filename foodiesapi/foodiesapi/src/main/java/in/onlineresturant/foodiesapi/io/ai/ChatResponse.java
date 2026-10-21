package in.onlineresturant.foodiesapi.io.ai;

import in.onlineresturant.foodiesapi.io.FoodResponse;
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
public class ChatResponse {
    private String conversationId;
    private String message;
    @Builder.Default
    private List<FoodResponse> foodRecommendations = new ArrayList<>();
    private ProposedOrderResponse proposedOrder;
    private NutritionSummaryResponse nutritionSummary;
    @Builder.Default
    private List<String> suggestedActions = new ArrayList<>();
}
