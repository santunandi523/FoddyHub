package in.onlineresturant.foodiesapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "proposed_orders")
public class ProposedOrderEntity {
    @Id
    private String id;
    private String userId; // Authenticated user ID if logged in
    private String sessionId; // Guest session ID if not logged in
    
    @Builder.Default
    private List<ProposedOrderItem> items = new ArrayList<>();
    
    // Server-side verified totals
    private double subtotal;
    private double estimatedTax;
    private double estimatedShipping;
    private double estimatedTotal;
    
    private NutritionSummary nutritionSummary;
    
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
