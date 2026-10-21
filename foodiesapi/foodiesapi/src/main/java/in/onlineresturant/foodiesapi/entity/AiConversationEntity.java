package in.onlineresturant.foodiesapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_conversations")
public class AiConversationEntity {
    @Id
    private String id;
    private String userId; // Authenticated user ID
    private String sessionId; // Fallback session ID
    
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();
    
    @Builder.Default
    private Instant createdAt = Instant.now();
    
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role; // "user", "assistant", "system", "tool"
        private String content;
        @Builder.Default
        private Instant timestamp = Instant.now();
    }
}
