package in.onlineresturant.foodiesapi.controller;

import in.onlineresturant.foodiesapi.io.UserRequest;
import in.onlineresturant.foodiesapi.io.UserResponse;
import in.onlineresturant.foodiesapi.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api")

public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)

    public UserResponse registerUser(@RequestBody UserRequest userRequest) {
        UserResponse userResponse = userService.registerUser(userRequest);
        return userResponse;
    }
}
