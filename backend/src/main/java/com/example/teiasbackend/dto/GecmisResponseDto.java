package com.example.teiasbackend.dto;

import com.example.teiasbackend.entity.BasvuruDurumu;
import com.example.teiasbackend.entity.BasvuruGecmisi;

import java.time.LocalDateTime;

public record GecmisResponseDto(
        Long id,
        BasvuruDurumu eskiDurum,
        BasvuruDurumu yeniDurum,
        String yeniDurumEtiketi,
        String aciklama,
        String islemYapan,
        LocalDateTime islemTarihi
) {
    public static GecmisResponseDto from(BasvuruGecmisi g) {
        return new GecmisResponseDto(g.getId(), g.getEskiDurum(), g.getYeniDurum(), g.getYeniDurum().getEtiket(),
                g.getAciklama(), g.getIslemYapan(), g.getIslemTarihi());
    }
}
