package in.onlineresturant.foodiesapi.config;

import in.onlineresturant.foodiesapi.entity.FoodEntity;
import in.onlineresturant.foodiesapi.entity.NutritionInfo;
import in.onlineresturant.foodiesapi.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FoodDataInitializer implements CommandLineRunner {

    private final FoodRepository foodRepository;

    @Override
    public void run(String... args) {
        try {
            long count = foodRepository.count();
            if (count == 0) {
                log.info("Database is empty. Seeding initial dishes with nutritional data...");
                seedInitialDishes();
            } else {
                log.info("Checking and enriching existing {} food items with nutritional metadata if missing...", count);
                enrichExistingDishes();
            }
        } catch (Exception e) {
            log.warn("Database initialization check skipped or failed: {}", e.getMessage());
        }
    }

    private void seedInitialDishes() {
        List<FoodEntity> sampleFoods = List.of(
                FoodEntity.builder()
                        .name("Hyderabadi Chicken Biryani")
                        .description("Fragrant basmati rice cooked with succulent spiced chicken, saffron, and aromatic herbs.")
                        .price(280.0)
                        .category("Biriyani")
                        .imageUrl("https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=600")
                        .available(true)
                        .isVegetarian(false)
                        .spiceLevel("Spicy")
                        .nutrition(NutritionInfo.builder()
                                .calories(580.0)
                                .protein(34.0)
                                .carbohydrates(62.0)
                                .fat(22.0)
                                .fiber(3.5)
                                .sugar(2.0)
                                .servingSize(350.0)
                                .servingUnit("g")
                                .isVegetarian(false)
                                .allergens(List.of("Dairy"))
                                .dataSource("Restaurant Verified Standard")
                                .isVerified(true)
                                .build())
                        .build(),

                FoodEntity.builder()
                        .name("Paneer Butter Masala")
                        .description("Rich and creamy cottage cheese cubes simmered in a mildly spiced tomato-butter gravy.")
                        .price(220.0)
                        .category("Biriyani") // or Indian Mains
                        .imageUrl("https://images.unsplash.com/photo-1631452180519-c014fe946bc7?w=600")
                        .available(true)
                        .isVegetarian(true)
                        .spiceLevel("Medium")
                        .nutrition(NutritionInfo.builder()
                                .calories(430.0)
                                .protein(18.0)
                                .carbohydrates(16.0)
                                .fat(32.0)
                                .fiber(4.0)
                                .sugar(6.0)
                                .servingSize(300.0)
                                .servingUnit("g")
                                .isVegetarian(true)
                                .allergens(List.of("Dairy", "Nuts"))
                                .dataSource("Standard Recipe Laboratory")
                                .isVerified(true)
                                .build())
                        .build(),

                FoodEntity.builder()
                        .name("Veggie Farmhouse Pizza")
                        .description("Crisp hand-tossed crust loaded with bell peppers, olives, mushrooms, sweet corn, and mozzarella.")
                        .price(260.0)
                        .category("Pizza")
                        .imageUrl("https://images.unsplash.com/photo-1513104890138-7c749659a591?w=600")
                        .available(true)
                        .isVegetarian(true)
                        .spiceLevel("Mild")
                        .nutrition(NutritionInfo.builder()
                                .calories(480.0)
                                .protein(16.0)
                                .carbohydrates(64.0)
                                .fat(18.0)
                                .fiber(5.0)
                                .sugar(5.0)
                                .servingSize(250.0)
                                .servingUnit("g")
                                .isVegetarian(true)
                                .allergens(List.of("Gluten", "Dairy"))
                                .dataSource("USDA Standard Recipe")
                                .isVerified(true)
                                .build())
                        .build(),

                FoodEntity.builder()
                        .name("Crispy Chicken Burger")
                        .description("Tender golden fried chicken patty with lettuce, tangy mayo, and cheddar on a toasted sesame bun.")
                        .price(180.0)
                        .category("Burger")
                        .imageUrl("https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600")
                        .available(true)
                        .isVegetarian(false)
                        .spiceLevel("Medium")
                        .nutrition(NutritionInfo.builder()
                                .calories(510.0)
                                .protein(28.0)
                                .carbohydrates(45.0)
                                .fat(24.0)
                                .fiber(2.5)
                                .sugar(6.0)
                                .servingSize(220.0)
                                .servingUnit("g")
                                .isVegetarian(false)
                                .allergens(List.of("Gluten", "Egg", "Dairy"))
                                .dataSource("Standard Fast Casual Analysis")
                                .isVerified(true)
                                .build())
                        .build(),

                FoodEntity.builder()
                        .name("Grilled Chicken Protein Salad")
                        .description("Fresh baby greens, grilled chicken strips, cherry tomatoes, cucumbers, and a light vinaigrette.")
                        .price(190.0)
                        .category("Salads")
                        .imageUrl("https://images.unsplash.com/photo-1540420773420-3366772f4999?w=600")
                        .available(true)
                        .isVegetarian(false)
                        .spiceLevel("Mild")
                        .nutrition(NutritionInfo.builder()
                                .calories(290.0)
                                .protein(36.0)
                                .carbohydrates(12.0)
                                .fat(10.0)
                                .fiber(4.5)
                                .sugar(3.0)
                                .servingSize(280.0)
                                .servingUnit("g")
                                .isVegetarian(false)
                                .allergens(List.of())
                                .dataSource("Dietary Nutrition Lab")
                                .isVerified(true)
                                .build())
                        .build(),

                FoodEntity.builder()
                        .name("Spicy Paneer Tikka Roll")
                        .description("Smoky tandoori paneer cubes wrapped in whole wheat flatbread with mint chutney and pickled onions.")
                        .price(140.0)
                        .category("Rolls")
                        .imageUrl("https://images.unsplash.com/photo-1626777552726-4a6b54c97e46?w=600")
                        .available(true)
                        .isVegetarian(true)
                        .spiceLevel("Spicy")
                        .nutrition(NutritionInfo.builder()
                                .calories(340.0)
                                .protein(15.0)
                                .carbohydrates(38.0)
                                .fat(14.0)
                                .fiber(4.0)
                                .sugar(3.0)
                                .servingSize(180.0)
                                .servingUnit("g")
                                .isVegetarian(true)
                                .allergens(List.of("Gluten", "Dairy"))
                                .dataSource("Standard Recipe Laboratory")
                                .isVerified(true)
                                .build())
                        .build(),

                FoodEntity.builder()
                        .name("Chocolate Lava Cake")
                        .description("Warm molten chocolate cake with a gooey center.")
                        .price(120.0)
                        .category("Cake")
                        .imageUrl("https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=600")
                        .available(true)
                        .isVegetarian(true)
                        .spiceLevel("Mild")
                        .nutrition(NutritionInfo.builder()
                                .calories(360.0)
                                .protein(5.0)
                                .carbohydrates(46.0)
                                .fat(18.0)
                                .fiber(2.0)
                                .sugar(32.0)
                                .servingSize(120.0)
                                .servingUnit("g")
                                .isVegetarian(true)
                                .allergens(List.of("Gluten", "Dairy", "Soy"))
                                .dataSource("Bakery Standard Recipe")
                                .isVerified(true)
                                .build())
                        .build()
        );

        foodRepository.saveAll(sampleFoods);
        log.info("Successfully seeded {} sample dishes with verified nutritional metrics.", sampleFoods.size());
    }

    private void enrichExistingDishes() {
        List<FoodEntity> foods = foodRepository.findAll();
        boolean modified = false;

        for (FoodEntity food : foods) {
            boolean dishUpdated = false;

            if (food.getAvailable() == null) {
                food.setAvailable(true);
                dishUpdated = true;
            }

            String nameLower = food.getName() != null ? food.getName().toLowerCase() : "";
            String catLower = food.getCategory() != null ? food.getCategory().toLowerCase() : "";

            if (food.getIsVegetarian() == null) {
                boolean isNonVeg = nameLower.contains("chicken") || nameLower.contains("mutton") || nameLower.contains("fish") || nameLower.contains("egg");
                food.setIsVegetarian(!isNonVeg);
                dishUpdated = true;
            }

            if (food.getSpiceLevel() == null) {
                if (nameLower.contains("spicy") || nameLower.contains("tikka") || nameLower.contains("masala") || nameLower.contains("biryani")) {
                    food.setSpiceLevel("Spicy");
                } else if (catLower.contains("cake") || catLower.contains("icecreame") || catLower.contains("salad")) {
                    food.setSpiceLevel("Mild");
                } else {
                    food.setSpiceLevel("Medium");
                }
                dishUpdated = true;
            }

            if (food.getNutrition() == null) {
                double baseCal = 400.0;
                double baseProt = 15.0;
                if (nameLower.contains("biryani")) { baseCal = 550.0; baseProt = 28.0; }
                else if (nameLower.contains("salad")) { baseCal = 220.0; baseProt = 20.0; }
                else if (nameLower.contains("pizza")) { baseCal = 490.0; baseProt = 17.0; }
                else if (nameLower.contains("burger")) { baseCal = 480.0; baseProt = 22.0; }
                else if (nameLower.contains("cake")) { baseCal = 350.0; baseProt = 4.0; }

                food.setNutrition(NutritionInfo.builder()
                        .calories(baseCal)
                        .protein(baseProt)
                        .carbohydrates(45.0)
                        .fat(18.0)
                        .fiber(3.0)
                        .sugar(5.0)
                        .servingSize(250.0)
                        .servingUnit("g")
                        .isVegetarian(food.getIsVegetarian())
                        .allergens(List.of())
                        .dataSource("Estimated from Standard Recipe Guidelines")
                        .isVerified(false)
                        .build());
                dishUpdated = true;
            }

            if (dishUpdated) {
                foodRepository.save(food);
                modified = true;
            }
        }

        if (modified) {
            log.info("Enriched existing dishes with nutritional metadata.");
        }
    }
}
