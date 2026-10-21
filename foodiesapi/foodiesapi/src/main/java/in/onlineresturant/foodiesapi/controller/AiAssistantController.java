package in.onlineresturant.foodiesapi.controller;

import in.onlineresturant.foodiesapi.entity.AiConversationEntity;
import in.onlineresturant.foodiesapi.io.ai.ChatRequest;
import in.onlineresturant.foodiesapi.io.ai.ChatResponse;
import in.onlineresturant.foodiesapi.io.ai.ModifyProposedOrderRequest;
import in.onlineresturant.foodiesapi.io.ai.ProposedOrderResponse;
import in.onlineresturant.foodiesapi.service.ProposedOrderService;
import in.onlineresturant.foodiesapi.service.UserService;
import in.onlineresturant.foodiesapi.service.ai.AiAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;
    private final ProposedOrderService proposedOrderService;
    private final UserService userService;

    private String getLoggedInUserIdOrNull() {
        try {
            return userService.findByUserId();
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String userId = getLoggedInUserIdOrNull();
        ChatResponse response = aiAssistantService.handleChat(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<AiConversationEntity> getConversation(@PathVariable String conversationId,
                                                                 @RequestParam(required = false) String sessionId) {
        String userId = getLoggedInUserIdOrNull();
        AiConversationEntity conversation = aiAssistantService.getConversation(conversationId, userId, sessionId);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/proposed-order")
    public ResponseEntity<ProposedOrderResponse> getProposedOrder(@RequestParam(required = false) String sessionId) {
        String userId = getLoggedInUserIdOrNull();
        ProposedOrderResponse order = proposedOrderService.getProposedOrder(userId, sessionId);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/proposed-order")
    public ResponseEntity<ProposedOrderResponse> modifyProposedOrder(@RequestBody ModifyProposedOrderRequest request,
                                                                     @RequestParam(required = false) String sessionId) {
        String userId = getLoggedInUserIdOrNull();
        ProposedOrderResponse order = proposedOrderService.modifyProposedOrder(userId, sessionId, request);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/proposed-order")
    public ResponseEntity<Map<String, String>> clearProposedOrder(@RequestParam(required = false) String sessionId) {
        String userId = getLoggedInUserIdOrNull();
        proposedOrderService.clearProposedOrder(userId, sessionId);
        return ResponseEntity.ok(Map.of("message", "Proposed order cleared successfully"));
    }

    @PostMapping("/proposed-order/transfer-to-cart")
    public ResponseEntity<Map<String, Object>> transferToCart(@RequestParam(required = false) String sessionId) {
        String userId = getLoggedInUserIdOrNull();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Please login to add this proposed meal to your real cart."
            ));
        }

        int count = proposedOrderService.transferToCart(userId, sessionId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Proposed meal items successfully transferred to your shopping cart!",
                "transferredItemsCount", count
        ));
    }
}
