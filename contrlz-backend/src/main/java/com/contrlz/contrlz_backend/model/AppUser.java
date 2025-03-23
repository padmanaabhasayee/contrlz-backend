package com.contrlz.contrlz_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Document(collection = "users") // Defines MongoDB collection
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AppUser implements UserDetails {

    @Id
    private String id;
    private String username;
    private String password;
    private String role = "USER"; // e.g., "ADMIN", "USER"
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> role);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

}
