package com.smartfin.backend.controller;

import com.smartfin.backend.dto.RegisterRequest;
import com.smartfin.backend.model.User;
import com.smartfin.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request);
            return ResponseEntity.ok(java.util.Collections.singletonMap("message", "User registered successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody com.smartfin.backend.dto.LoginRequest request) {
        try {
            User user = userService.verifyUser(request.getUsername(), request.getPassword());
            // Return JSON so frontend parses it correctly
            return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Logged in successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(java.util.Collections.singletonMap("error", "Invalid credentials"));
        }
    }
}
