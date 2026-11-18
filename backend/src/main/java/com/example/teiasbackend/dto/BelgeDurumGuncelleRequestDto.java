package com.example.teiasbackend.dto;

import com.example.teiasbackend.entity.BelgeDurumu;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BelgeDurumGuncelleRequestDto(
        @NotNull(message = "Belge durumu zorunludur.") BelgeDurumu durum,
        @Size(max = 500) String not
) {
}
