package in.onlineresturant.foodiesapi.service.ai;

import in.onlineresturant.foodiesapi.entity.AiConversationEntity;
import in.onlineresturant.foodiesapi.io.ai.ChatRequest;
import in.onlineresturant.foodiesapi.io.ai.ChatResponse;

public interface AiAssistantService {
    ChatResponse handleChat(ChatRequest request, String userId);
    AiConversationEntity getConversation(String conversationId, String userId, String sessionId);
}
