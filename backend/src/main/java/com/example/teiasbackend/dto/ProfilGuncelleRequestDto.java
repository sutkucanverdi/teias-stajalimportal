package com.example.teiasbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfilGuncelleRequestDto(
        @NotBlank(message = "E-posta zorunludur.") @Email(message = "Geçerli bir e-posta adresi giriniz.") @Size(max = 150)
        String email,
        @NotBlank(message = "Telefon zorunludur.")
        @Pattern(regexp = "^0?5\\d{9}$", message = "Telefon 05XXXXXXXXX formatında olmalıdır.")
        String telefon
) {
}
