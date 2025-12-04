package com.cinnamon.backend.controller;

import com.cinnamon.backend.model.RegistrationRequest;
import com.cinnamon.backend.service.AuthService;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationRequest registrationRequest) {
        try {
            String userId = authService.registerUser(registrationRequest);
            return ResponseEntity.ok("User registered with UID: " + userId);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
