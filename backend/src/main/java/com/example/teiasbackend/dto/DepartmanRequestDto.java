package com.example.teiasbackend.dto;

import jakarta.validation.constraints.*;

public record DepartmanRequestDto(
        @NotBlank(message = "Birim adı zorunludur.") @Size(max = 150) String ad,
        @Size(max = 2000) String aciklama,
        @NotBlank(message = "Şehir zorunludur.") @Size(max = 60) String sehir,
        @NotNull(message = "Kontenjan zorunludur.") @Min(value = 0, message = "Kontenjan negatif olamaz.") @Max(1000) Integer kontenjan,
        Boolean aktif
) {
}
