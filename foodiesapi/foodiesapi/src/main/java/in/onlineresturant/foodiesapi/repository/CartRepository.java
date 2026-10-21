package in.onlineresturant.foodiesapi.repository;

import in.onlineresturant.foodiesapi.entity.CartEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartRepository extends MongoRepository<CartEntity,String> {
    Optional<CartEntity> findByUserId(String userId);
    void deleteByUserId(String userId);
}
