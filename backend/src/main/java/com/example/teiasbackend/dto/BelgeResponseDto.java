package com.example.teiasbackend.dto;

import com.example.teiasbackend.entity.BasvuruBelgesi;
import com.example.teiasbackend.entity.BelgeDurumu;

import java.time.LocalDateTime;

public record BelgeResponseDto(
        Long id,
        Long belgeTuruId,
        String belgeTuruKod,
        String belgeTuruAd,
        String orijinalAd,
        String icerikTuru,
        long boyut,
        BelgeDurumu durum,
        String yoneticiNotu,
        LocalDateTime yuklemeTarihi
) {
    public static BelgeResponseDto from(BasvuruBelgesi b) {
        return new BelgeResponseDto(b.getId(), b.getBelgeTuru().getId(), b.getBelgeTuru().getKod(),
                b.getBelgeTuru().getAd(), b.getOrijinalAd(), b.getIcerikTuru(), b.getBoyut(),
                b.getDurum(), b.getYoneticiNotu(), b.getYuklemeTarihi());
    }
}
