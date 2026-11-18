package com.example.teiasbackend.dto;

import com.example.teiasbackend.entity.BelgeTuru;

public record BelgeTuruResponseDto(
        Long id,
        String kod,
        String ad,
        String aciklama,
        boolean zorunlu,
        int maxBoyutMb,
        boolean aktif
) {
    public static BelgeTuruResponseDto from(BelgeTuru b) {
        return new BelgeTuruResponseDto(b.getId(), b.getKod(), b.getAd(), b.getAciklama(),
                b.isZorunlu(), b.getMaxBoyutMb(), b.isAktif());
    }
}
