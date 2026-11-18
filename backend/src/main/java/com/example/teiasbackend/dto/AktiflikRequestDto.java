package com.example.teiasbackend.dto;

import jakarta.validation.constraints.NotNull;

public record AktiflikRequestDto(@NotNull(message = "Aktiflik bilgisi zorunludur.") Boolean aktif) {
}
