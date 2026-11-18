package com.example.teiasbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SifreDegistirRequestDto(
        @NotBlank(message = "Mevcut şifre zorunludur.") String mevcutSifre,

        @NotBlank(message = "Yeni şifre zorunludur.")
        @Size(min = 8, max = 64, message = "Şifre 8-64 karakter arasında olmalıdır.")
        @Pattern(regexp = "^(?=.*[A-Za-zÇĞİÖŞÜçğıöşü])(?=.*\\d).+$", message = "Şifre en az bir harf ve bir rakam içermelidir.")
        String yeniSifre
) {
}
