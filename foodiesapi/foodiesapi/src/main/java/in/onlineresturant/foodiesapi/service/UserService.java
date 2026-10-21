package in.onlineresturant.foodiesapi.service;

import in.onlineresturant.foodiesapi.io.UserRequest;
import in.onlineresturant.foodiesapi.io.UserResponse;

public interface UserService {
    UserResponse registerUser(UserRequest request);
    String findByUserId();
}
