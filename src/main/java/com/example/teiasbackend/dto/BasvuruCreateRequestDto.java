package com.example.teiasbackend.dto;

import lombok.Data;
@Data
public class BasvuruCreateRequestDto {
    private Long userId;
    private String tcKimlik;
    private String adSoyad;
    private String eposta;
    private String telefon;

    private String okulAdi;
    private String alan;
    private String dal;
    private String sinif;
    private Double notOrtalamasi;

    private String stajTuru;
    private String departman;
    private String sehir;
    private String baslamaTarihi;
    private String bitisTarihi;
}