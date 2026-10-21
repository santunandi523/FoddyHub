package in.onlineresturant.foodiesapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposedOrderItem {
    private String foodId;
    private String name;
    private String category;
    private String imageUrl;
    private double unitPrice;
    private int quantity;
    private double itemSubtotal; // unitPrice * quantity
    private NutritionInfo nutrition;
    private Boolean isVegetarian;
    private String spiceLevel;
}
