package com.example.teiasbackend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BasvuruResponseDto {
    private Long id;
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
    private String isgBelgesiYolu;
    private String okulKabulFormuYolu;
    private String veliIzinBelgesiYolu;
    private String durum;
    private LocalDateTime basvuruTarihi;
}