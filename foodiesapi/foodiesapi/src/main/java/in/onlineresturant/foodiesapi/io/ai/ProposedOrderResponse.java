package in.onlineresturant.foodiesapi.io.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposedOrderResponse {
    private String id;
    private String userId;
    private String sessionId;
    @Builder.Default
    private List<ProposedOrderItemResponse> items = new ArrayList<>();
    private double subtotal; // Verified server-side
    private double estimatedTax; // 10%
    private double estimatedShipping; // ₹10
    private double estimatedTotal; // subtotal + tax + shipping
    private boolean isEstimatedTotal; // true to indicate taxes/shipping follow standard checkout rules
    private NutritionSummaryResponse nutritionSummary;
    private Instant updatedAt;
}
