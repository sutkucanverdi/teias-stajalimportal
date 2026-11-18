package com.example.teiasbackend.dto;

import com.example.teiasbackend.entity.StajTuru;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/** Öğrencinin başvuru oluşturma / taslak güncelleme isteği. Kişisel bilgiler giriş yapan kullanıcıdan alınır. */
public record BasvuruCreateRequestDto(
        @NotBlank(message = "Okul adı zorunludur.") @Size(max = 200) String okulAdi,
        @NotBlank(message = "Alan zorunludur.") @Size(max = 120) String alan,
        @Size(max = 120) String dal,
        @NotBlank(message = "Sınıf zorunludur.") @Pattern(regexp = "^(9|10|11|12)$", message = "Sınıf 9, 10, 11 veya 12 olmalıdır.") String sinif,
        @DecimalMin(value = "0.0", message = "Not ortalaması 0-100 arasında olmalıdır.")
        @DecimalMax(value = "100.0", message = "Not ortalaması 0-100 arasında olmalıdır.")
        Double notOrtalamasi,
        @NotNull(message = "Staj türü zorunludur.") StajTuru stajTuru,
        @NotBlank(message = "Şehir zorunludur.") @Size(max = 60) String sehir,
        @NotNull(message = "Tercih edilen birim zorunludur.") Long tercihDepartmanId,
        @NotNull(message = "Başlama tarihi zorunludur.") LocalDate baslamaTarihi,
        @NotNull(message = "Bitiş tarihi zorunludur.") LocalDate bitisTarihi,
        @Size(max = 1000, message = "Not en fazla 1000 karakter olabilir.") String ogrenciNotu
) {
    @AssertTrue(message = "Bitiş tarihi başlama tarihinden sonra olmalıdır.")
    public boolean isTarihAraligiGecerli() {
        return baslamaTarihi == null || bitisTarihi == null || bitisTarihi.isAfter(baslamaTarihi);
    }
}
