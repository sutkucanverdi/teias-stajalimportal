package com.example.teiasbackend.dto;

import com.example.teiasbackend.entity.Rol;
import jakarta.validation.constraints.*;

/** İK tarafından personel (İK / Birim sorumlusu) hesabı açmak için. */
public record PersonelCreateRequestDto(
        @NotBlank(message = "Ad zorunludur.") @Size(max = 100) String ad,
        @NotBlank(message = "Soyad zorunludur.") @Size(max = 100) String soyad,
        @NotBlank(message = "Kullanıcı adı zorunludur.")
        @Pattern(regexp = "^[a-z0-9._]{3,50}$", message = "Kullanıcı adı 3-50 karakter; küçük harf, rakam, nokta ve alt çizgi içerebilir.")
        String kullaniciAdi,
        @Email(message = "Geçerli bir e-posta adresi giriniz.") @Size(max = 150) String email,
        @Size(max = 20) String telefon,
        @NotBlank(message = "Şifre zorunludur.") @Size(min = 8, max = 64, message = "Şifre 8-64 karakter arasında olmalıdır.") String sifre,
        @NotNull(message = "Rol zorunludur.") Rol rol,
        Long departmanId
) {
}
