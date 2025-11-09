package com.smartspend.auth.dtos;

public record AuthResponseDto(
    Long id,
    String token,
    String username,
    String email
) {
    
}