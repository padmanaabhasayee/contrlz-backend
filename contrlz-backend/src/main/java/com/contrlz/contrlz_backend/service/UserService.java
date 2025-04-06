package com.contrlz.contrlz_backend.service;

import com.contrlz.contrlz_backend.controller.WebSocketController;
import com.contrlz.contrlz_backend.model.AppUser;
import com.contrlz.contrlz_backend.repository.UserRepository;
import com.contrlz.contrlz_backend.security.JWTService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper;

    @Autowired
    private WebSocketController webSocketController;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public UserService() {
        this.objectMapper = new ObjectMapper();
    }

    public ResponseEntity<?> registerUser(AppUser user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }

        String pass = Objects.requireNonNullElse(user.getPassword(), "omsrisairam");
        user.setPassword(passwordEncoder.encode(pass)); // Hash the password before storing
        userRepository.save(user);

        webSocketController.sendUsersUpdate();
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

        return ResponseEntity.ok(response);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                    user.getUsername(), user.getPassword(), user.getAuthorities());
    }

    public ResponseEntity<String> updateUserRole(String id, String newRole) {
        Optional<AppUser> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            AppUser user = optionalUser.get();
            user.setRole(newRole);
            userRepository.save(user);
            webSocketController.sendUsersUpdate();
            return ResponseEntity.ok("User role updated successfully");
        }
        return ResponseEntity.badRequest().body("User not found");
    }

    public ResponseEntity<Map<String, Object>> changePassword(String username, String oldPassword, String newPassword) {
        Optional<AppUser> existingUser = userRepository.findByUsername(username);
        Map<String, Object> response = new HashMap<>();

        if (existingUser.isPresent()) {
            AppUser user = existingUser.get();

            // Verify old password
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                response.put("success", false);
                response.put("message", "Old password is incorrect");
                return ResponseEntity.status(400).body(response);
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Generate new token
            String token = JWTService.generateToken(user.getUsername(), user.getRole(), 86400000); // 1-day expiration
            response.put("success", true);
            response.put("message", "Password changed successfully");
            response.put("token", token);

            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.status(404).body(response);
        }
    }

    public ResponseEntity<String> bulkUpdateUserRoles(List<String> userIds, String newRole) {
        try {
            if (!List.of("ADMIN", "USER").contains(newRole)) {
                return ResponseEntity.badRequest().body("Invalid role specified");
            }

            List<AppUser> users = userRepository.findAllById(userIds);
            if (users.size() != userIds.size()) {
                return ResponseEntity.badRequest().body("Some users not found");
            }

            users.forEach(user -> user.setRole(newRole));
            userRepository.saveAll(users);
            webSocketController.sendUsersUpdate();
            return ResponseEntity.ok("Roles updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating roles: " + e.getMessage());
        }
    }

    public ResponseEntity<String> bulkResetPassword(List<String> userIds) {
        try {
            List<AppUser> users = userRepository.findAllById(userIds);
            if (users.size() != userIds.size()) {
                return ResponseEntity.badRequest().body("Some users not found");
            }

            users.forEach(user -> {
                user.setPassword(passwordEncoder.encode("omsrisairam"));
            });
            userRepository.saveAll(users);

            return ResponseEntity.ok("Passwords reset successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error resetting passwords: " + e.getMessage());
        }
    }

    public ResponseEntity<String> resetPassword(String userId) {
        try {
            Optional<AppUser> userOptional = userRepository.findById(userId);

            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            AppUser user = userOptional.get();
            user.setPassword(passwordEncoder.encode("omsrisairam"));
            userRepository.save(user);

            return ResponseEntity.ok("Password reset successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error resetting password: " + e.getMessage());
        }
    }

    public List<AppUser> bulkCreateUsers(List<AppUser> users){
        List<AppUser> processedUsers = new ArrayList<>();

        for (AppUser user : users) {
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode("omsrisairam"));
            }
            processedUsers.add(user);
        }

        List<AppUser> savedUsers = userRepository.saveAll(processedUsers);
        webSocketController.sendUsersUpdate();
        return savedUsers;
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
        webSocketController.sendUsersUpdate();
    }

    public ResponseEntity<String> bulkDeleteUsers(Map<String, List<String>> request) {
        List<String> userIds = request.get("userIds");

        if (userIds == null || userIds.isEmpty()) {
            return ResponseEntity.badRequest().body("User IDs list is empty or missing.");
        }

        userRepository.deleteAllById(userIds);
        webSocketController.sendUsersUpdate();
        return ResponseEntity.ok("Users deleted successfully.");
    }
}