package in.onlineresturant.foodiesapi.controller;

import in.onlineresturant.foodiesapi.io.FoodRequest;
import in.onlineresturant.foodiesapi.io.FoodResponse;
import in.onlineresturant.foodiesapi.service.FoodService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@AllArgsConstructor
@CrossOrigin("*")

public class FoodController {

    private final FoodService foodService;

    @PostMapping
    public FoodResponse addFood(@RequestPart("food") String foodString,@RequestPart("file") MultipartFile file){
        ObjectMapper objectMapper = new ObjectMapper();
        FoodRequest request= null;
        try {
            request= objectMapper.readValue(foodString,FoodRequest.class);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        FoodResponse response=foodService.addFood(request,file);
        return response;
    }

    @GetMapping
    public List<FoodResponse> readFood(){
        return foodService.readFood();
    }
    @GetMapping("/{id}")
    public FoodResponse readFood(@PathVariable String id){
        return foodService.readFood(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFood(@PathVariable String id){
        foodService.deleteFood(id);
    }

}
