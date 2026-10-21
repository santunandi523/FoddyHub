package in.onlineresturant.foodiesapi.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class FoodResponse {

    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private double price;
    private String category;
    private Boolean available;
    private Boolean isVegetarian;
    private String spiceLevel;
    private in.onlineresturant.foodiesapi.entity.NutritionInfo nutrition;
}
