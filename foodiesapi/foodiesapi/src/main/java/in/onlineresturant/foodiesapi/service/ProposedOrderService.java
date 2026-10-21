package in.onlineresturant.foodiesapi.service;

import in.onlineresturant.foodiesapi.entity.ProposedOrderEntity;
import in.onlineresturant.foodiesapi.entity.ProposedOrderItem;
import in.onlineresturant.foodiesapi.io.ai.ModifyProposedOrderRequest;
import in.onlineresturant.foodiesapi.io.ai.ProposedOrderResponse;

import java.util.List;

public interface ProposedOrderService {
    ProposedOrderEntity getOrCreateEntity(String userId, String sessionId);
    ProposedOrderResponse getProposedOrder(String userId, String sessionId);
    ProposedOrderResponse modifyProposedOrder(String userId, String sessionId, ModifyProposedOrderRequest request);
    ProposedOrderResponse setProposedOrderItems(String userId, String sessionId, List<ProposedOrderItem> items);
    void clearProposedOrder(String userId, String sessionId);
    ProposedOrderResponse convertToResponse(ProposedOrderEntity entity);
    int transferToCart(String userId, String sessionId);
}
