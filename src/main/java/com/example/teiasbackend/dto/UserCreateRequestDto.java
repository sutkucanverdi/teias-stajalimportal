package com.example.teiasbackend.dto;

import lombok.Data;

@Data
public class UserCreateRequestDto {
    private String tcKimlik;
    private String ad;
    private String soyad;
    private String sifre;
}