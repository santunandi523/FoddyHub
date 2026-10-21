package in.onlineresturant.foodiesapi.service;

import in.onlineresturant.foodiesapi.entity.CartEntity;
import in.onlineresturant.foodiesapi.entity.FoodEntity;
import in.onlineresturant.foodiesapi.entity.NutritionSummary;
import in.onlineresturant.foodiesapi.entity.ProposedOrderEntity;
import in.onlineresturant.foodiesapi.entity.ProposedOrderItem;
import in.onlineresturant.foodiesapi.io.ai.ModifyProposedOrderRequest;
import in.onlineresturant.foodiesapi.io.ai.ProposedOrderItemResponse;
import in.onlineresturant.foodiesapi.io.ai.ProposedOrderResponse;
import in.onlineresturant.foodiesapi.repository.CartRepository;
import in.onlineresturant.foodiesapi.repository.FoodRepository;
import in.onlineresturant.foodiesapi.repository.ProposedOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProposedOrderServiceImpl implements ProposedOrderService {

    private final ProposedOrderRepository proposedOrderRepository;
    private final FoodRepository foodRepository;
    private final CartRepository cartRepository;
    private final NutritionService nutritionService;
    private final FoodSearchService foodSearchService;

    @Override
    public ProposedOrderEntity getOrCreateEntity(String userId, String sessionId) {
        Optional<ProposedOrderEntity> entityOpt = Optional.empty();
        if (StringUtils.hasText(userId)) {
            entityOpt = proposedOrderRepository.findByUserId(userId);
        } else if (StringUtils.hasText(sessionId)) {
            entityOpt = proposedOrderRepository.findBySessionId(sessionId);
        }

        if (entityOpt.isPresent()) {
            return entityOpt.get();
        }

        ProposedOrderEntity newEntity = ProposedOrderEntity.builder()
                .userId(userId)
                .sessionId(sessionId)
                .items(new ArrayList<>())
                .subtotal(0.0)
                .estimatedShipping(0.0)
                .estimatedTax(0.0)
                .estimatedTotal(0.0)
                .updatedAt(Instant.now())
                .build();

        return proposedOrderRepository.save(newEntity);
    }

    @Override
    public ProposedOrderResponse getProposedOrder(String userId, String sessionId) {
        ProposedOrderEntity entity = getOrCreateEntity(userId, sessionId);
        recalculateAndSave(entity);
        return convertToResponse(entity);
    }

    @Override
    public ProposedOrderResponse modifyProposedOrder(String userId, String sessionId, ModifyProposedOrderRequest request) {
        ProposedOrderEntity entity = getOrCreateEntity(userId, sessionId);
        List<ProposedOrderItem> items = entity.getItems() != null ? entity.getItems() : new ArrayList<>();

        if (request.getAction() == null) {
            recalculateAndSave(entity);
            return convertToResponse(entity);
        }

        switch (request.getAction()) {
            case CLEAR -> items.clear();

            case ADD -> {
                FoodEntity food = resolveFood(request.getFoodId(), request.getFoodName());
                if (food != null && !Boolean.FALSE.equals(food.getAvailable())) {
                    int addQty = request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1;
                    Optional<ProposedOrderItem> existing = items.stream()
                            .filter(i -> i.getFoodId().equals(food.getId()))
                            .findFirst();
                    if (existing.isPresent()) {
                        existing.get().setQuantity(existing.get().getQuantity() + addQty);
                    } else {
                        items.add(createProposedItem(food, addQty));
                    }
                }
            }

            case REMOVE -> {
                FoodEntity food = resolveFood(request.getFoodId(), request.getFoodName());
                if (food != null) {
                    items.removeIf(i -> i.getFoodId().equals(food.getId()));
                } else if (StringUtils.hasText(request.getFoodName())) {
                    String nameLower = request.getFoodName().trim().toLowerCase();
                    items.removeIf(i -> i.getName() != null && i.getName().toLowerCase().contains(nameLower));
                }
            }

            case UPDATE_QTY -> {
                FoodEntity food = resolveFood(request.getFoodId(), request.getFoodName());
                if (food != null) {
                    int newQty = request.getQuantity() != null ? request.getQuantity() : 0;
                    if (newQty <= 0) {
                        items.removeIf(i -> i.getFoodId().equals(food.getId()));
                    } else {
                        items.stream()
                                .filter(i -> i.getFoodId().equals(food.getId()))
                                .findFirst()
                                .ifPresent(i -> i.setQuantity(newQty));
                    }
                }
            }

            case REPLACE -> {
                FoodEntity oldFood = resolveFood(request.getFoodId(), request.getFoodName());
                FoodEntity newFood = resolveFood(request.getReplaceWithFoodId(), request.getReplaceWithFoodName());

                if (newFood != null && !Boolean.FALSE.equals(newFood.getAvailable())) {
                    int qty = 1;
                    if (oldFood != null) {
                        Optional<ProposedOrderItem> oldItem = items.stream()
                                .filter(i -> i.getFoodId().equals(oldFood.getId()))
                                .findFirst();
                        if (oldItem.isPresent()) {
                            qty = oldItem.get().getQuantity();
                            items.remove(oldItem.get());
                        }
                    } else if (items.size() > 1 && StringUtils.hasText(request.getFoodName()) && request.getFoodName().contains("second")) {
                        // "Replace the second dish"
                        qty = items.get(1).getQuantity();
                        items.remove(1);
                    } else if (!items.isEmpty()) {
                        qty = items.get(0).getQuantity();
                        items.remove(0);
                    }
                    items.add(createProposedItem(newFood, qty));
                }
            }

            case SET_ITEMS -> {
                // Kept as-is
            }
        }

        entity.setItems(items);
        recalculateAndSave(entity);
        return convertToResponse(entity);
    }

    @Override
    public ProposedOrderResponse setProposedOrderItems(String userId, String sessionId, List<ProposedOrderItem> items) {
        ProposedOrderEntity entity = getOrCreateEntity(userId, sessionId);
        entity.setItems(items != null ? items : new ArrayList<>());
        recalculateAndSave(entity);
        return convertToResponse(entity);
    }

    @Override
    public void clearProposedOrder(String userId, String sessionId) {
        ProposedOrderEntity entity = getOrCreateEntity(userId, sessionId);
        entity.getItems().clear();
        recalculateAndSave(entity);
    }

    @Override
    public int transferToCart(String userId, String sessionId) {
        ProposedOrderEntity entity = getOrCreateEntity(userId, sessionId);
        if (entity.getItems() == null || entity.getItems().isEmpty()) {
            return 0;
        }

        if (!StringUtils.hasText(userId)) {
            throw new IllegalStateException("Authentication required to transfer proposed meal to shopping cart.");
        }

        Optional<CartEntity> cartOpt = cartRepository.findByUserId(userId);
        CartEntity cart = cartOpt.orElseGet(() -> new CartEntity(userId, new HashMap<>()));
        Map<String, Integer> cartItems = cart.getItems() != null ? cart.getItems() : new HashMap<>();

        int transferredCount = 0;
        for (ProposedOrderItem item : entity.getItems()) {
            if (item.getFoodId() != null && item.getQuantity() > 0) {
                cartItems.put(item.getFoodId(), cartItems.getOrDefault(item.getFoodId(), 0) + item.getQuantity());
                transferredCount += item.getQuantity();
            }
        }

        cart.setItems(cartItems);
        cartRepository.save(cart);
        return transferredCount;
    }

    @Override
    public ProposedOrderResponse convertToResponse(ProposedOrderEntity entity) {
        if (entity == null) return null;

        List<ProposedOrderItemResponse> itemResponses = entity.getItems() != null
                ? entity.getItems().stream().map(this::convertItemToResponse).collect(Collectors.toList())
                : new ArrayList<>();

        return ProposedOrderResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .sessionId(entity.getSessionId())
                .items(itemResponses)
                .subtotal(round(entity.getSubtotal()))
                .estimatedShipping(round(entity.getEstimatedShipping()))
                .estimatedTax(round(entity.getEstimatedTax()))
                .estimatedTotal(round(entity.getEstimatedTotal()))
                .isEstimatedTotal(true)
                .nutritionSummary(nutritionService.convertToResponse(entity.getNutritionSummary()))
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ProposedOrderItemResponse convertItemToResponse(ProposedOrderItem item) {
        return ProposedOrderItemResponse.builder()
                .foodId(item.getFoodId())
                .name(item.getName())
                .category(item.getCategory())
                .imageUrl(item.getImageUrl())
                .unitPrice(round(item.getUnitPrice()))
                .quantity(item.getQuantity())
                .itemSubtotal(round(item.getItemSubtotal()))
                .nutrition(item.getNutrition())
                .isVegetarian(item.getIsVegetarian())
                .spiceLevel(item.getSpiceLevel())
                .build();
    }

    private void recalculateAndSave(ProposedOrderEntity entity) {
        List<ProposedOrderItem> items = entity.getItems();
        double subtotal = 0.0;

        if (items != null) {
            Iterator<ProposedOrderItem> it = items.iterator();
            while (it.hasNext()) {
                ProposedOrderItem item = it.next();
                FoodEntity freshFood = foodRepository.findById(item.getFoodId()).orElse(null);
                if (freshFood == null || Boolean.FALSE.equals(freshFood.getAvailable())) {
                    // Drop unavailable or deleted food
                    it.remove();
                    continue;
                }
                // Strictly use verified server-side price
                item.setName(freshFood.getName());
                item.setCategory(freshFood.getCategory());
                item.setImageUrl(freshFood.getImageUrl());
                item.setUnitPrice(freshFood.getPrice());
                item.setItemSubtotal(freshFood.getPrice() * item.getQuantity());
                item.setNutrition(freshFood.getNutrition());
                item.setIsVegetarian(freshFood.getIsVegetarian());
                item.setSpiceLevel(freshFood.getSpiceLevel());

                subtotal += item.getItemSubtotal();
            }
        }

        // Standard application checkout pricing: Shipping ₹10, Tax 10%
        double shipping = subtotal > 0 ? 10.0 : 0.0;
        double tax = subtotal * 0.10;
        double total = subtotal + shipping + tax;

        entity.setSubtotal(round(subtotal));
        entity.setEstimatedShipping(round(shipping));
        entity.setEstimatedTax(round(tax));
        entity.setEstimatedTotal(round(total));
        entity.setNutritionSummary(nutritionService.calculateSummaryFromProposedItems(items));
        entity.setUpdatedAt(Instant.now());

        proposedOrderRepository.save(entity);
    }

    private ProposedOrderItem createProposedItem(FoodEntity food, int qty) {
        return ProposedOrderItem.builder()
                .foodId(food.getId())
                .name(food.getName())
                .category(food.getCategory())
                .imageUrl(food.getImageUrl())
                .unitPrice(food.getPrice())
                .quantity(qty)
                .itemSubtotal(food.getPrice() * qty)
                .nutrition(food.getNutrition())
                .isVegetarian(food.getIsVegetarian())
                .spiceLevel(food.getSpiceLevel())
                .build();
    }

    private FoodEntity resolveFood(String id, String name) {
        if (StringUtils.hasText(id)) {
            Optional<FoodEntity> byId = foodRepository.findById(id.trim());
            if (byId.isPresent()) return byId.get();
        }
        if (StringUtils.hasText(name)) {
            return foodSearchService.findFoodByIdOrName(name.trim());
        }
        return null;
    }

    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
