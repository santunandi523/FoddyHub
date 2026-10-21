package in.onlineresturant.foodiesapi.repository;

import in.onlineresturant.foodiesapi.entity.ProposedOrderEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProposedOrderRepository extends MongoRepository<ProposedOrderEntity, String> {
    Optional<ProposedOrderEntity> findByUserId(String userId);
    Optional<ProposedOrderEntity> findBySessionId(String sessionId);
    void deleteByUserId(String userId);
    void deleteBySessionId(String sessionId);
}
