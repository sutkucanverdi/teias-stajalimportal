package com.example.teiasbackend.dto;

import lombok.Data;

@Data // Lombok: Get, Set kodlarını bizim için yazıyor. (Burada @Entity VEYA @Table YOK! Çünkü bu veritabanında bir tablo değil, sadece kurye paketi!)
public class UserCreateRequestDto {
    private String tcKimlik;
    private String adSoyad;
    private String sifre;
}