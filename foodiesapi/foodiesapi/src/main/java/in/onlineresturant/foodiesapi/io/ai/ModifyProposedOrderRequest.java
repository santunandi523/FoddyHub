package in.onlineresturant.foodiesapi.io.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyProposedOrderRequest {
    public enum Action {
        ADD,
        REMOVE,
        REPLACE,
        UPDATE_QTY,
        CLEAR,
        SET_ITEMS
    }

    private Action action;
    private String foodId;
    private String foodName;
    private String replaceWithFoodId;
    private String replaceWithFoodName;
    private Integer quantity;
}
