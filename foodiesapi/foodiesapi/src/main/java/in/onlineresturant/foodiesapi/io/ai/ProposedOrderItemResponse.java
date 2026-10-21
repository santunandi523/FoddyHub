package in.onlineresturant.foodiesapi.io.ai;

import in.onlineresturant.foodiesapi.entity.NutritionInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposedOrderItemResponse {
    private String foodId;
    private String name;
    private String category;
    private String imageUrl;
    private double unitPrice;
    private int quantity;
    private double itemSubtotal;
    private NutritionInfo nutrition;
    private Boolean isVegetarian;
    private String spiceLevel;
}
