package in.onlineresturant.foodiesapi.service;

import in.onlineresturant.foodiesapi.io.FoodRequest;
import in.onlineresturant.foodiesapi.io.FoodResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FoodService {
    String uploadFile(MultipartFile file);
    FoodResponse addFood(FoodRequest request, MultipartFile file);
    List<FoodResponse> readFood();
    FoodResponse readFood(String id);
    boolean deleteFile(String filename);
    void deleteFood(String id);
}
