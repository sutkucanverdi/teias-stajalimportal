package com.example.teiasbackend.dto;

import com.example.teiasbackend.validation.TcKimlikNo;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record StudentRegisterRequestDto(
        @NotBlank(message = "TC Kimlik No zorunludur.")
        @TcKimlikNo
        String tcKimlik,

        @NotBlank(message = "Ad zorunludur.") @Size(max = 100)
        String ad,

        @NotBlank(message = "Soyad zorunludur.") @Size(max = 100)
        String soyad,

        @NotBlank(message = "E-posta zorunludur.") @Email(message = "Geçerli bir e-posta adresi giriniz.") @Size(max = 150)
        String email,

        @NotBlank(message = "Telefon zorunludur.")
        @Pattern(regexp = "^0?5\\d{9}$", message = "Telefon 05XXXXXXXXX formatında olmalıdır.")
        String telefon,

        @NotNull(message = "Doğum tarihi zorunludur.") @Past(message = "Doğum tarihi geçmiş bir tarih olmalıdır.")
        LocalDate dogumTarihi,

        @NotBlank(message = "Şifre zorunludur.")
        @Size(min = 8, max = 64, message = "Şifre 8-64 karakter arasında olmalıdır.")
        @Pattern(regexp = "^(?=.*[A-Za-zÇĞİÖŞÜçğıöşü])(?=.*\\d).+$", message = "Şifre en az bir harf ve bir rakam içermelidir.")
        String sifre
) {
}
