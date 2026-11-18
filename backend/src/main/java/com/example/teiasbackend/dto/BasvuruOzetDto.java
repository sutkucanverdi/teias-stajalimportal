package com.example.teiasbackend.dto;

import com.example.teiasbackend.entity.Basvuru;
import com.example.teiasbackend.entity.BasvuruDurumu;
import com.example.teiasbackend.entity.StajTuru;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Liste ekranları için hafif başvuru görünümü */
public record BasvuruOzetDto(
        Long id,
        String ogrenciAdSoyad,
        String tcKimlik,
        String okulAdi,
        String alan,
        String sinif,
        String sehir,
        StajTuru stajTuru,
        String tercihDepartmanAdi,
        String atananDepartmanAdi,
        BasvuruDurumu durum,
        String durumEtiketi,
        LocalDate baslamaTarihi,
        LocalDate bitisTarihi,
        LocalDateTime basvuruTarihi,
        LocalDateTime guncellemeTarihi
) {
    public static BasvuruOzetDto from(Basvuru b) {
        return new BasvuruOzetDto(
                b.getId(), b.getUser().getAdSoyad(), b.getUser().getTcKimlik(), b.getOkulAdi(), b.getAlan(),
                b.getSinif(), b.getSehir(), b.getStajTuru(),
                b.getTercihDepartman() != null ? b.getTercihDepartman().getAd() : null,
                b.getAtananDepartman() != null ? b.getAtananDepartman().getAd() : null,
                b.getDurum(), b.getDurum().getEtiket(), b.getBaslamaTarihi(), b.getBitisTarihi(),
                b.getBasvuruTarihi(), b.getGuncellemeTarihi()
        );
    }
}
