package com.smartspend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

@Service
public class SmartSpendUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        String password = user.getUserHashedPassword();
        if (password == null || password.isEmpty()) {
            password = ""; // Spring Security requiere password no null
        }
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserEmail())
                .password(password)
                .roles("USER") // Rol básico para todos los usuarios registrados
                .build();
    }
}