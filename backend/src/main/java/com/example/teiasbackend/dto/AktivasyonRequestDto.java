package com.example.teiasbackend.dto;

import jakarta.validation.constraints.NotBlank;

public record AktivasyonRequestDto(
        @NotBlank(message = "TC Kimlik No zorunludur.") String kullaniciAdi,
        @NotBlank(message = "Aktivasyon kodu zorunludur.") String kod
) {
}
