package com.contrlz.contrlz_backend.controller;

import com.contrlz.contrlz_backend.model.AppUser;
import com.contrlz.contrlz_backend.repository.UserRepository;
import com.contrlz.contrlz_backend.security.JWTService;
import com.contrlz.contrlz_backend.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private WebSocketController webSocketController;
    // Get all users (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<AppUser>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@RequestBody AppUser user){
        return userService.registerUser(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bulk-create-user")
    public ResponseEntity<?> bulkCreateUsers(@RequestBody List<AppUser> users) {
        if (users == null || users.isEmpty()) {
            return ResponseEntity.badRequest().body("User list is empty or missing.");
        }

        List<AppUser> savedUsers = userService.bulkCreateUsers(users);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUsers);
    }


    // Register User
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AppUser user) {
        return userService.registerUser(user);
    }

    // Login to get JWT Token
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AppUser user) {
        return userService.login(user);
    }

    // Change Password
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        return userService.changePassword(username, oldPassword, newPassword);
    }

    // Update User Role (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update-role")
    public ResponseEntity<String> updateUserRole(@RequestBody Map<String, String> request) {
        String id = request.get("id");
        String newRole = request.get("role");
        return userService.updateUserRole(id, newRole);
    }

    // Bulk Update Roles (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bulk-update-role")
    public ResponseEntity<String> bulkUpdateUserRoles(@RequestBody Map<String, Object> request) {
        List<String> userIds = (List<String>) request.get("userIds");
        String newRole = (String) request.get("role");
        return userService.bulkUpdateUserRoles(userIds, newRole);
    }

    // Reset Password (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        return userService.resetPassword(userId);
    }

    // Bulk Reset Passwords (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bulk-reset-password")
    public ResponseEntity<String> bulkResetPassword(@RequestBody Map<String, Object> request) {
        List<String> userIds = (List<String>) request.get("userIds");
        return userService.bulkResetPassword(userIds);
    }

    // Get user by username
    @GetMapping("/{username}")
    public ResponseEntity<AppUser> getUser(@PathVariable String username) {
        Optional<AppUser> user = userRepository.findByUsername(username);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{username}")
    public ResponseEntity<String> updateUser(
            @PathVariable String username,
            @RequestBody Map<String, String> request) {
        String newUsername = request.get("newUsername");
        String email = request.get("email");
        Optional<AppUser> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AppUser user = userOptional.get();
        user.setUsername(newUsername);
        user.setEmail(email);

        userRepository.save(user);
        webSocketController.sendUsersUpdate();
        String token = JWTService.generateToken(user.getUsername(), user.getRole(), 86400000); // 1-day expiration
        return ResponseEntity.ok(token);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete-user")
    public void deleteUser (@RequestParam String userId){
        userService.deleteUser(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bulk-delete-user")
    public ResponseEntity<String> bulkDeleteUsers(@RequestBody Map<String, List<String>> request) {
        return userService.bulkDeleteUsers(request);
    }

}