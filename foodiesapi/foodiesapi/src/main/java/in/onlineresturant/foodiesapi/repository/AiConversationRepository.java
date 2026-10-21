package in.onlineresturant.foodiesapi.repository;

import in.onlineresturant.foodiesapi.entity.AiConversationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiConversationRepository extends MongoRepository<AiConversationEntity, String> {
    Optional<AiConversationEntity> findByIdAndUserId(String id, String userId);
    Optional<AiConversationEntity> findByIdAndSessionId(String id, String sessionId);
    Optional<AiConversationEntity> findTopByUserIdOrderByUpdatedAtDesc(String userId);
    Optional<AiConversationEntity> findTopBySessionIdOrderByUpdatedAtDesc(String sessionId);
}
