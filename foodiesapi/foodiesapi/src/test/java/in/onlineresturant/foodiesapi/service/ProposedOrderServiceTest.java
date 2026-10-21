package in.onlineresturant.foodiesapi.service;

import in.onlineresturant.foodiesapi.entity.FoodEntity;
import in.onlineresturant.foodiesapi.entity.ProposedOrderEntity;
import in.onlineresturant.foodiesapi.io.ai.ModifyProposedOrderRequest;
import in.onlineresturant.foodiesapi.io.ai.ProposedOrderResponse;
import in.onlineresturant.foodiesapi.repository.CartRepository;
import in.onlineresturant.foodiesapi.repository.FoodRepository;
import in.onlineresturant.foodiesapi.repository.ProposedOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProposedOrderServiceTest {

    @Mock
    private ProposedOrderRepository proposedOrderRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private NutritionService nutritionService;

    @Mock
    private FoodSearchService foodSearchService;

    @InjectMocks
    private ProposedOrderServiceImpl proposedOrderService;

    private FoodEntity pizza;
    private FoodEntity paneer;

    @BeforeEach
    void setUp() {
        pizza = FoodEntity.builder()
                .id("pizza-1")
                .name("Veggie Pizza")
                .price(250.0)
                .available(true)
                .build();

        paneer = FoodEntity.builder()
                .id("paneer-1")
                .name("Paneer Tikka")
                .price(200.0)
                .available(true)
                .build();
    }

    @Test
    void testModifyProposedOrder_AddAndRecalculate() {
        ProposedOrderEntity entity = ProposedOrderEntity.builder()
                .userId("user-1")
                .items(new ArrayList<>())
                .build();

        when(proposedOrderRepository.findByUserId("user-1")).thenReturn(Optional.of(entity));
        when(foodRepository.findById("pizza-1")).thenReturn(Optional.of(pizza));
        when(proposedOrderRepository.save(any(ProposedOrderEntity.class))).thenAnswer(i -> i.getArgument(0));

        ModifyProposedOrderRequest req = ModifyProposedOrderRequest.builder()
                .action(ModifyProposedOrderRequest.Action.ADD)
                .foodId("pizza-1")
                .quantity(2)
                .build();

        ProposedOrderResponse res = proposedOrderService.modifyProposedOrder("user-1", null, req);

        assertNotNull(res);
        assertEquals(1, res.getItems().size());
        assertEquals("Veggie Pizza", res.getItems().get(0).getName());
        assertEquals(2, res.getItems().get(0).getQuantity());
        assertEquals(500.0, res.getSubtotal()); // 2 * 250
        assertEquals(10.0, res.getEstimatedShipping());
        assertEquals(50.0, res.getEstimatedTax()); // 10% of 500
        assertEquals(560.0, res.getEstimatedTotal()); // 500 + 10 + 50
    }

    @Test
    void testModifyProposedOrder_Replace() {
        ProposedOrderEntity entity = ProposedOrderEntity.builder()
                .userId("user-1")
                .items(new ArrayList<>())
                .build();

        when(proposedOrderRepository.findByUserId("user-1")).thenReturn(Optional.of(entity));
        when(foodRepository.findById("pizza-1")).thenReturn(Optional.of(pizza));
        when(foodRepository.findById("paneer-1")).thenReturn(Optional.of(paneer));
        when(proposedOrderRepository.save(any(ProposedOrderEntity.class))).thenAnswer(i -> i.getArgument(0));

        // First add pizza
        proposedOrderService.modifyProposedOrder("user-1", null, ModifyProposedOrderRequest.builder()
                .action(ModifyProposedOrderRequest.Action.ADD)
                .foodId("pizza-1")
                .quantity(1)
                .build());

        // Now replace pizza with paneer
        ModifyProposedOrderRequest replaceReq = ModifyProposedOrderRequest.builder()
                .action(ModifyProposedOrderRequest.Action.REPLACE)
                .foodId("pizza-1")
                .replaceWithFoodId("paneer-1")
                .build();

        ProposedOrderResponse res = proposedOrderService.modifyProposedOrder("user-1", null, replaceReq);

        assertEquals(1, res.getItems().size());
        assertEquals("Paneer Tikka", res.getItems().get(0).getName());
        assertEquals(200.0, res.getSubtotal());
        assertEquals(10.0, res.getEstimatedShipping());
        assertEquals(20.0, res.getEstimatedTax());
        assertEquals(230.0, res.getEstimatedTotal());
    }
}
