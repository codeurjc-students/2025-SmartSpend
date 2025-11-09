package com.smartspend.auth;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartspend.auth.dtos.AuthResponseDto;
import com.smartspend.auth.dtos.LoginRequestDto;
import com.smartspend.auth.dtos.RegisterRequestDto;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;


@Service
public class AuthService {
    
    @Autowired
    UserRepository userRepository;

    @Autowired 
    PasswordEncoder passwordEncoder;

    @Autowired 
    JwtService jwtService; 

    @Autowired 
    AuthenticationManager authenticationManager;

    public AuthResponseDto register(RegisterRequestDto req) {

        if (validateRequest(req) == false) {
            throw new IllegalArgumentException("Invalid registration request");
        } 
        if (userRepository.findByUserEmail(req.email()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        User u = new User();
        u.setUserName(req.username());
        u.setUserEmail(req.email());
        u.setUserHashedPassword(passwordEncoder.encode(req.password()));
        userRepository.save(u);

        // generate JWT token
        String token = jwtService.generateToken(u.getUserId(), u.getUserEmail());
        

        AuthResponseDto authResponse = new AuthResponseDto(
            u.getUserId(),
            token,
            u.getUserName(),
            u.getUserEmail()
        );
        return authResponse; 
    
    }

    
  public AuthResponseDto login(LoginRequestDto req) {

    System.out.println("Attempting login for email: " + req.email());

    try {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );
        

    
        Optional<User> userOptional = userRepository.findByUserEmail(req.email());

        System.out.println("User found: " + userOptional.isPresent());

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found after successful authentication.");
        }

        User u = userOptional.get();

        

        String token = jwtService.generateToken(u.getUserId(), u.getUserEmail());

         System.out.println("🔑 Token generated: " + token);

        AuthResponseDto res = new AuthResponseDto(
            u.getUserId(),
            token,
            u.getUserName(),
            u.getUserEmail()
        );
        
        return res;
    
    } catch (AuthenticationException e){
        throw new IllegalArgumentException("Invalid credentials", e);
    }
  }




    private boolean validateRequest(RegisterRequestDto req) {
        if (req.username() == null || req.username().isEmpty()) {
            return false;
        }
        if (req.email() == null || req.email().isEmpty()) {
            return false;
        }
        if (req.password() == null || req.password().isEmpty()) {
            return false;
        }
        // Additional validation can be added here
        return true;
    }


}
