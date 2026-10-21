package in.onlineresturant.foodiesapi.service;

import in.onlineresturant.foodiesapi.entity.FoodEntity;
import in.onlineresturant.foodiesapi.entity.NutritionInfo;
import in.onlineresturant.foodiesapi.entity.ProposedOrderEntity;
import in.onlineresturant.foodiesapi.io.FoodResponse;
import in.onlineresturant.foodiesapi.io.ai.FoodSearchCriteria;
import in.onlineresturant.foodiesapi.io.ai.MealPlanRequest;
import in.onlineresturant.foodiesapi.io.ai.ProposedOrderResponse;
import in.onlineresturant.foodiesapi.repository.FoodRepository;
import in.onlineresturant.foodiesapi.repository.ProposedOrderRepository;
import in.onlineresturant.foodiesapi.service.ai.AiToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiFeatureTest {

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private NutritionService nutritionService;

    @Mock
    private ProposedOrderService proposedOrderService;

    @InjectMocks
    private FoodSearchServiceImpl foodSearchService;

    private FoodEntity biryani;
    private FoodEntity paneer;
    private FoodEntity pizza;

    @BeforeEach
    void setUp() {
        biryani = FoodEntity.builder()
                .id("b-1")
                .name("Hyderabadi Chicken Biryani")
                .description("Spicy aromatic biryani")
                .price(280.0)
                .category("Biriyani")
                .available(true)
                .isVegetarian(false)
                .spiceLevel("Spicy")
                .nutrition(NutritionInfo.builder().calories(580.0).protein(34.0).isVerified(true).build())
                .build();

        paneer = FoodEntity.builder()
                .id("p-1")
                .name("Paneer Butter Masala")
                .description("Creamy mild paneer")
                .price(220.0)
                .category("Biriyani")
                .available(true)
                .isVegetarian(true)
                .spiceLevel("Medium")
                .nutrition(NutritionInfo.builder().calories(430.0).protein(18.0).isVerified(true).build())
                .build();

        pizza = FoodEntity.builder()
                .id("pz-1")
                .name("Veggie Pizza")
                .description("Cheesy mild pizza")
                .price(260.0)
                .category("Pizza")
                .available(true)
                .isVegetarian(true)
                .spiceLevel("Mild")
                .nutrition(NutritionInfo.builder().calories(480.0).protein(16.0).isVerified(true).build())
                .build();
    }

    @Test
    void testFoodSearch_BudgetAndDietaryFilter() {
        when(foodRepository.findAll()).thenReturn(List.of(biryani, paneer, pizza));

        // Search vegetarian under 250
        FoodSearchCriteria criteria = FoodSearchCriteria.builder()
                .isVegetarian(true)
                .maxPrice(250.0)
                .availableOnly(true)
                .build();

        List<FoodEntity> results = foodSearchService.searchFoods(criteria);

        assertEquals(1, results.size());
        assertEquals("Paneer Butter Masala", results.get(0).getName());
    }

    @Test
    void testFoodSearch_SpicePreference() {
        when(foodRepository.findAll()).thenReturn(List.of(biryani, paneer, pizza));

        FoodSearchCriteria criteria = FoodSearchCriteria.builder()
                .spicePreference("spicy")
                .build();

        List<FoodEntity> results = foodSearchService.searchFoods(criteria);

        assertEquals(1, results.size());
        assertEquals("Hyderabadi Chicken Biryani", results.get(0).getName());
    }

    @Test
    void testFindFoodByIdOrName() {
        when(foodRepository.findById("b-1")).thenReturn(Optional.of(biryani));
        when(foodRepository.findAll()).thenReturn(List.of(biryani, paneer, pizza));

        FoodEntity byId = foodSearchService.findFoodByIdOrName("b-1");
        assertNotNull(byId);
        assertEquals("Hyderabadi Chicken Biryani", byId.getName());

        FoodEntity byName = foodSearchService.findFoodByIdOrName("paneer");
        assertNotNull(byName);
        assertEquals("Paneer Butter Masala", byName.getName());
    }
}
