package com.example.teiasbackend.dto;

import lombok.Data;

@Data
public class UserCreateRequestDto {
    private String tcKimlik;
    private String adSoyad;
    private String sifre;
}