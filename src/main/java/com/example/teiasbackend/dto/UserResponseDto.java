package com.example.teiasbackend.dto;

import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String tcKimlik;
    private String ad;
    private String soyad;
    private String rol;
}