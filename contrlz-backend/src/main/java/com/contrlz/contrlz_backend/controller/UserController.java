package com.contrlz.contrlz_backend.controller;

import com.contrlz.contrlz_backend.model.AppUser;
import com.contrlz.contrlz_backend.security.JWTService;
import com.contrlz.contrlz_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    // Register User
    @PostMapping("/register")
    public ResponseEntity<ResponseEntity<?>> registerUser(@RequestBody AppUser user) {
        ResponseEntity<?> savedUser = userService.registerUser(user);
        return ResponseEntity.ok(savedUser);
    }

    // Login to get JWT Token
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AppUser user) {
        return userService.login(user);
    }
}
