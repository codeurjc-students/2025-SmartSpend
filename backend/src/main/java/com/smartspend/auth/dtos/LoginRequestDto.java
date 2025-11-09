package com.smartspend.auth.dtos;


public record LoginRequestDto(
    String email, 
    String password) {
    
}