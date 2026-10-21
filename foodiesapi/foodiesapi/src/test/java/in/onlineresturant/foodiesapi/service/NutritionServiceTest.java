package in.onlineresturant.foodiesapi.service;

import in.onlineresturant.foodiesapi.entity.FoodEntity;
import in.onlineresturant.foodiesapi.entity.NutritionInfo;
import in.onlineresturant.foodiesapi.entity.NutritionSummary;
import in.onlineresturant.foodiesapi.io.ai.NutritionSummaryResponse;
import in.onlineresturant.foodiesapi.repository.FoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class NutritionServiceTest {

    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private NutritionServiceImpl nutritionService;

    private FoodEntity biryani;
    private FoodEntity salad;

    @BeforeEach
    void setUp() {
        biryani = FoodEntity.builder()
                .id("food-1")
                .name("Chicken Biryani")
                .price(250.0)
                .nutrition(NutritionInfo.builder()
                        .calories(550.0)
                        .protein(30.0)
                        .carbohydrates(60.0)
                        .fat(20.0)
                        .fiber(4.0)
                        .sugar(2.0)
                        .isVerified(true)
                        .build())
                .build();

        salad = FoodEntity.builder()
                .id("food-2")
                .name("Green Salad")
                .price(120.0)
                .nutrition(NutritionInfo.builder()
                        .calories(150.0)
                        .protein(5.0)
                        .carbohydrates(10.0)
                        .fat(3.0)
                        .fiber(6.0)
                        .sugar(4.0)
                        .isVerified(false)
                        .build())
                .build();
    }

    @Test
    void testNutritionCalculation_AccurateSumAndMultipliers() {
        Map<FoodEntity, Integer> items = new HashMap<>();
        items.put(biryani, 2); // 2 * 550 = 1100 kcal, 2 * 30 = 60g protein
        items.put(salad, 1);   // 1 * 150 = 150 kcal, 1 * 5 = 5g protein

        NutritionSummary summary = nutritionService.calculateSummary(items);

        assertNotNull(summary);
        assertEquals(1250.0, summary.getTotalCalories());
        assertEquals(65.0, summary.getTotalProtein());
        assertEquals(130.0, summary.getTotalCarbohydrates());
        assertEquals(43.0, summary.getTotalFat());
        assertEquals(14.0, summary.getTotalFiber());
        assertTrue(summary.getIsCompleteData());
        assertFalse(summary.getIsVerified()); // Because salad is estimated
    }

    @Test
    void testNutritionCalculation_FlagsMissingData() {
        FoodEntity unmeasuredDish = FoodEntity.builder()
                .id("food-3")
                .name("Chef Special Mystery")
                .price(300.0)
                .nutrition(null) // No nutrition
                .build();

        Map<FoodEntity, Integer> items = new HashMap<>();
        items.put(biryani, 1);
        items.put(unmeasuredDish, 1);

        NutritionSummary summary = nutritionService.calculateSummary(items);

        assertNotNull(summary);
        assertEquals(550.0, summary.getTotalCalories());
        assertFalse(summary.getIsCompleteData(), "Should flag incomplete data when a dish lacks nutrition");
        assertEquals(1, summary.getMissingNutritionDishes().size());
        assertEquals("Chef Special Mystery", summary.getMissingNutritionDishes().get(0));

        NutritionSummaryResponse response = nutritionService.convertToResponse(summary);
        assertNotNull(response.getNote());
        assertTrue(response.getNote().contains("Nutrition information unavailable"));
    }

    @Test
    void testFilterByNutritionConstraints() {
        List<FoodEntity> list = List.of(biryani, salad);

        // Under 200 calories
        List<FoodEntity> under200 = nutritionService.filterByNutritionConstraints(list, 200.0, null);
        assertEquals(1, under200.size());
        assertEquals("Green Salad", under200.get(0).getName());

        // At least 25g protein
        List<FoodEntity> highProtein = nutritionService.filterByNutritionConstraints(list, null, 25.0);
        assertEquals(1, highProtein.size());
        assertEquals("Chicken Biryani", highProtein.get(0).getName());
    }
}
