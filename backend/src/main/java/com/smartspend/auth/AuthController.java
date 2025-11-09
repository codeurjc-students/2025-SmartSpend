package com.smartspend.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartspend.auth.dtos.AuthResponseDto;
import com.smartspend.auth.dtos.LoginRequestDto;
import com.smartspend.auth.dtos.RegisterRequestDto;

@RestController
@RequestMapping("/api/v1/auth")

public class AuthController {
    
    @Autowired 
    private AuthService authService; 


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest) {
        System.out.println(" LOGIN request received in controller");
        AuthResponseDto token = authService.login(loginRequest);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto registerRequest) {
        AuthResponseDto response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
