package com.example.teiasbackend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

// 📤 SUNUM TEPSİMİZ: Dış dünyaya cevabı dönerken tüm User tablosunu değil, sadece stajyerin adını ve başvuru detaylarını vereceğiz!
@Data
@Builder
public class BasvuruResponseDto {
    private Long id;
    private String adSoyad;       // Stajyerin adı (User tablosundan çekip buraya koyacağız)
    private String departman;
    private String durum;         // "BEKLEMEDE", "ONAYLANDI" vb.
    private LocalDateTime basvuruTarihi;
}