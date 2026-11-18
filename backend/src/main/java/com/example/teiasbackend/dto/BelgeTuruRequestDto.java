package com.example.teiasbackend.dto;

import jakarta.validation.constraints.*;

public record BelgeTuruRequestDto(
        @NotBlank(message = "Kod zorunludur.")
        @Pattern(regexp = "^[A-Z0-9_]{2,40}$", message = "Kod büyük harf, rakam ve alt çizgiden oluşmalıdır (ör. ISG).")
        String kod,
        @NotBlank(message = "Belge adı zorunludur.") @Size(max = 150) String ad,
        @Size(max = 2000) String aciklama,
        @NotNull(message = "Zorunluluk bilgisi gereklidir.") Boolean zorunlu,
        @NotNull @Min(value = 1, message = "En az 1 MB olmalıdır.") @Max(value = 10, message = "En fazla 10 MB olabilir.") Integer maxBoyutMb,
        Boolean aktif
) {
}
