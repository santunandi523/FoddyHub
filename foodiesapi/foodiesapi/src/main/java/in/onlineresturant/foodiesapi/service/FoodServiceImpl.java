package in.onlineresturant.foodiesapi.service;


import in.onlineresturant.foodiesapi.entity.FoodEntity;
import in.onlineresturant.foodiesapi.io.FoodRequest;
import in.onlineresturant.foodiesapi.io.FoodResponse;
import in.onlineresturant.foodiesapi.repository.FoodRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FoodServiceImpl implements FoodService {

    @Autowired
    private  S3Client s3Client;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private  FoodRepository foodRepository;

    @Value("${aws.s3.bucketname}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file) {

        try {
            String filenameExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
            String key = UUID.randomUUID().toString() + "." + filenameExtension;
            if (bucketName == null || bucketName.isEmpty() || bucketName.contains("dummy")) {
                throw new RuntimeException("S3 credentials/bucket not configured.");
            }
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .acl("public-read")
                    .contentType(file.getContentType())
                    .build();
            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            if (response.sdkHttpResponse().isSuccessful()) {
                return "https://" + bucketName + ".s3.amazonaws.com/" + key;
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload Failed");
            }
        } catch (Exception e) {
            System.err.println("S3 upload failed or bypassed, falling back to dummy image URL: " + e.getMessage());
            return "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600";
        }
    }

    @Override
    public FoodResponse addFood(FoodRequest request, MultipartFile file) {
        FoodEntity newFoodEntity= convertToEntity(request);
        String imgUrl= uploadFile(file);
        newFoodEntity.setImageUrl(imgUrl);
        newFoodEntity= foodRepository.save(newFoodEntity);
        return convertToResponse(newFoodEntity);
    }

    @Override
    public List<FoodResponse> readFood() {

        List<FoodEntity> foodEntities = foodRepository.findAll();
        return foodEntities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public FoodResponse readFood(String id) {
        FoodEntity foodExisting= foodRepository.findById(id).orElseThrow(()->new RuntimeException("Food Not Found BY This Name"+id));

        return convertToResponse(foodExisting);

    }

    @Override
    public boolean deleteFile(String filename) {
        try {
            if (bucketName == null || bucketName.isEmpty() || bucketName.contains("dummy")) {
                return true;
            }
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            System.err.println("S3 delete failed: " + e.getMessage());
        }
        return true;
    }

    @Override
    public void deleteFood(String id) {
        FoodResponse response=readFood(id);
        String imgUrl=response.getImageUrl();
        String filename=imgUrl.substring(imgUrl.lastIndexOf("/")+1);
        if(deleteFile(filename)){
            foodRepository.deleteById(id);
        }
    }

    private FoodEntity convertToEntity(FoodRequest request){

        return FoodEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .build();
    }
    private FoodResponse convertToResponse(FoodEntity entity){
        return FoodResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl())
                .available(entity.getAvailable() != null ? entity.getAvailable() : true)
                .isVegetarian(entity.getIsVegetarian())
                .spiceLevel(entity.getSpiceLevel())
                .nutrition(entity.getNutrition())
                .build();
    }
}
