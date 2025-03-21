package com.contrlz.contrlz_backend.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Replace this with your actual user retrieval logic (e.g., from a database)
        if (!"admin".equals(username)) {
            throw new UsernameNotFoundException("User not found");
        }

        return User.withUsername("admin")
                .password("{noop}password") // No encryption, for testing only
                .roles("USER")
                .build();
    }
}
