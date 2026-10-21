package in.onlineresturant.foodiesapi.controller;

import in.onlineresturant.foodiesapi.io.AuthenticationRequest;
import in.onlineresturant.foodiesapi.io.AuthenticationResponse;
import in.onlineresturant.foodiesapi.service.AppUserDetailsService;
import in.onlineresturant.foodiesapi.util.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor

public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public AuthenticationResponse login(@RequestBody AuthenticationRequest request) {
        // Normalize email the same way registration does (lowercase + trim)
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword()));
        final UserDetails userDetails = appUserDetailsService.loadUserByUsername(normalizedEmail);
        final String jwtToken = jwtUtil.generateToken(userDetails);
        return new AuthenticationResponse(normalizedEmail, jwtToken);
    }


}
