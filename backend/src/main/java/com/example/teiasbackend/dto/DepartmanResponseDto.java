package com.example.teiasbackend.dto;

import com.example.teiasbackend.entity.Departman;

public record DepartmanResponseDto(
        Long id,
        String ad,
        String aciklama,
        String sehir,
        int kontenjan,
        boolean aktif,
        long onaylananSayisi,
        long kalanKontenjan
) {
    public static DepartmanResponseDto from(Departman d, long onaylanan) {
        return new DepartmanResponseDto(d.getId(), d.getAd(), d.getAciklama(), d.getSehir(),
                d.getKontenjan(), d.isAktif(), onaylanan, Math.max(0, d.getKontenjan() - onaylanan));
    }
}
