package com.example.teiasbackend.dto;

import jakarta.validation.constraints.NotBlank;

/** Öğrenciler TC Kimlik No veya e-posta, personel kurum kullanıcı adı ile giriş yapar. */
public record LoginRequestDto(
        @NotBlank(message = "Kullanıcı adı / TC Kimlik No zorunludur.") String kullaniciAdi,
        @NotBlank(message = "Şifre zorunludur.") String sifre
) {
}
