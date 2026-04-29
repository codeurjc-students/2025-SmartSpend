package com.smartspend.auth.dtos;

public record RegisterRequestDto(
    String email, 
    String password) {
}
