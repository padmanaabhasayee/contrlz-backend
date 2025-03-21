package com.contrlz.contrlz_backend.service;

import com.contrlz.contrlz_backend.model.AppUser;
import com.contrlz.contrlz_backend.repository.UserRepository;
import com.contrlz.contrlz_backend.security.JWTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authManager;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public ResponseEntity<?> registerUser(AppUser user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword())); // Hash the password before storing
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    public ResponseEntity<Map<String, Object>> login(AppUser user) {
        Map<String, Object> response = new HashMap<>();
        Optional<AppUser> existingUser = userRepository.findByUsername(user.getUsername());

        if (existingUser.isEmpty() || !passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) {
            response.put("success", false);
            response.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(response);
        }

        String token = JWTService.generateToken(existingUser.get().getUsername(), existingUser.get().getRole(), 86400000); // 1 day expiration
        response.put("success", true);
        response.put("token", token);
        response.put("role", existingUser.get().getRole());

        return ResponseEntity.ok(response);
    }

    public String verify(AppUser appUser) {
        try {
            Optional<AppUser> user = userRepository.findByUsername(appUser.getUsername());
            if (user.isEmpty()) {
                logger.error("User not found: {}", appUser.getUsername());
                return "fail";
            }
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.get().getUsername(), appUser.getPassword()));

            if (authentication.isAuthenticated()) {
                logger.info("User authenticated successfully: {} with role: {}", user.get().getUsername(), user.get().getRole());
                return JWTService.generateToken(user.get().getUsername(), user.get().getRole(), 86400000);
            } else {
                logger.warn("Authentication failed for user: {}", appUser.getUsername());
                return "fail";
            }
        } catch (Exception e) {
            logger.error("Error during authentication: {}", e.getMessage());
            return "fail";
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                    user.getUsername(), user.getPassword(), user.getAuthorities());
    }
}
