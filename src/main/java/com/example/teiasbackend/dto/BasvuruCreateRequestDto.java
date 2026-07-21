package com.example.teiasbackend.dto;

import lombok.Data;

// 📥 KURYEMİZ: Dışarıdan (Swagger veya React'ten) bir başvuru gelirken sadece bu iki veriyi isteyeceğiz!
@Data
public class BasvuruCreateRequestDto {
    private Long userId;     // Başvuruyu hangi stajyer yapıyor? (Örn: 8 numaralı ID)
    private String departman; // Hangi bölüme başvuruyor? (Örn: "Bilgi Teknolojileri")
}