package in.onlineresturant.foodiesapi.service.ai;

import in.onlineresturant.foodiesapi.entity.AiConversationEntity;

import java.util.List;

public interface LlmService {
    LlmExecutionResult processUserMessage(String userMessage,
                                          List<AiConversationEntity.ChatMessage> history,
                                          String userId,
                                          String sessionId);
}
