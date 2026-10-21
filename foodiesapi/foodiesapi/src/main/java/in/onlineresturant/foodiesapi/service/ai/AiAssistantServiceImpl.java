package in.onlineresturant.foodiesapi.service.ai;

import in.onlineresturant.foodiesapi.entity.AiConversationEntity;
import in.onlineresturant.foodiesapi.io.ai.ChatRequest;
import in.onlineresturant.foodiesapi.io.ai.ChatResponse;
import in.onlineresturant.foodiesapi.repository.AiConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private final AiConversationRepository conversationRepository;
    private final LlmService llmService;

    @Override
    public ChatResponse handleChat(ChatRequest request, String userId) {
        String sessionId = request.getSessionId();
        if (!StringUtils.hasText(sessionId) && !StringUtils.hasText(userId)) {
            sessionId = UUID.randomUUID().toString();
        }

        AiConversationEntity conversation = getOrCreateConversation(request.getConversationId(), userId, sessionId);

        // Append User Message
        AiConversationEntity.ChatMessage userMsg = AiConversationEntity.ChatMessage.builder()
                .role("user")
                .content(request.getMessage())
                .timestamp(Instant.now())
                .build();
        conversation.getMessages().add(userMsg);

        // Process message through LLM & backend tools
        LlmExecutionResult result = llmService.processUserMessage(
                request.getMessage(),
                conversation.getMessages(),
                userId,
                sessionId
        );

        // Append Assistant Message
        AiConversationEntity.ChatMessage assistantMsg = AiConversationEntity.ChatMessage.builder()
                .role("assistant")
                .content(result.getResponseText())
                .timestamp(Instant.now())
                .build();
        conversation.getMessages().add(assistantMsg);
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        return ChatResponse.builder()
                .conversationId(conversation.getId())
                .message(result.getResponseText())
                .foodRecommendations(result.getFoodRecommendations())
                .proposedOrder(result.getProposedOrder())
                .nutritionSummary(result.getNutritionSummary())
                .suggestedActions(result.getSuggestedActions())
                .build();
    }

    @Override
    public AiConversationEntity getConversation(String conversationId, String userId, String sessionId) {
        AiConversationEntity entity = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        // Validate Ownership
        if (StringUtils.hasText(userId)) {
            if (entity.getUserId() != null && !entity.getUserId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this conversation.");
            }
        } else if (StringUtils.hasText(sessionId)) {
            if (entity.getSessionId() != null && !entity.getSessionId().equals(sessionId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this conversation.");
            }
        }

        return entity;
    }

    private AiConversationEntity getOrCreateConversation(String conversationId, String userId, String sessionId) {
        if (StringUtils.hasText(conversationId)) {
            return getConversation(conversationId, userId, sessionId);
        }

        AiConversationEntity entity = AiConversationEntity.builder()
                .userId(userId)
                .sessionId(sessionId)
                .messages(new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return conversationRepository.save(entity);
    }
}
