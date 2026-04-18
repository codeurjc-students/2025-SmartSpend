package com.smartspend.auth;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import com.smartspend.auth.dtos.AuthResponseDto;
import com.smartspend.auth.dtos.GoogleTokenDto;
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

    @Value("${google.client.id}")
    private String googleClientId;

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
        } else {
            System.out.println("User details: " + userOptional.get().getUserName() + ", " + userOptional.get().getUserEmail());
        }

        User u = userOptional.get();

        

        String token = jwtService.generateToken(u.getUserId(), u.getUserEmail());

        //  System.out.println("🔑 Token generated: " + token);

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

  public AuthResponseDto googleLogin(GoogleTokenDto googleLoginRequest) {
    try {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
            .setAudience(Collections.singletonList(googleClientId))
            .build();

        GoogleIdToken idToken = verifier.verify(googleLoginRequest.token());

        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name"); 
            
            
            User user = userRepository.findByUserEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setUserEmail(email);
                newUser.setUserName(name);
                newUser.setUserHashedPassword(passwordEncoder.encode("google-oauth2-user")); // Placeholder password
                return userRepository.save(newUser);
            });

            String token = jwtService.generateToken(user.getUserId(), user.getUserEmail());
            return new AuthResponseDto(
                user.getUserId(),
                token,
                user.getUserName(),
                user.getUserEmail()
            );
        } else {
            throw new IllegalArgumentException("Invalid Google token");
        } 
    } catch (Exception e) {
        throw new RuntimeException("Error al verificar la identidad con Google: " + e.getMessage());
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
