package com.example.teiasbackend.dto;

import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String tcKimlik;
    private String adSoyad;
    private String rol;
    // DİKKAT: Burada "sifre" alanı kasıtlı olarak YOK! Müşteri veya tarayıcı asla şifreyi göremez.
}