package com.example.teiasbackend.dto;

public record LoginResponseDto(
        String mesaj,
        String token,
        String tokenTuru,
        long gecerlilikSaniye,
        UserResponseDto kullanici
) {
}
