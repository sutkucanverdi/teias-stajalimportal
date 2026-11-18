package com.example.teiasbackend.dto;

import com.example.teiasbackend.entity.BasvuruDurumu;
import com.example.teiasbackend.entity.StajTuru;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Başvuru detay görünümü */
public record BasvuruResponseDto(
        Long id,
        OgrenciBilgisi ogrenci,
        String okulAdi,
        String alan,
        String dal,
        String sinif,
        Double notOrtalamasi,
        StajTuru stajTuru,
        String sehir,
        LocalDate baslamaTarihi,
        LocalDate bitisTarihi,
        String ogrenciNotu,
        DepartmanOzet tercihDepartman,
        DepartmanOzet atananDepartman,
        BasvuruDurumu durum,
        String durumEtiketi,
        String degerlendirmeNotu,
        LocalDateTime basvuruTarihi,
        LocalDateTime olusturmaTarihi,
        LocalDateTime guncellemeTarihi,
        List<BelgeResponseDto> belgeler,
        /** Giriş yapan kullanıcının bu başvuruya uygulayabileceği durum geçişleri */
        List<BasvuruDurumu> yapilabilecekGecisler,
        boolean duzenlenebilir
) {
    public record OgrenciBilgisi(Long id, String ad, String soyad, String tcKimlik, String email,
                                 String telefon, LocalDate dogumTarihi) {
    }

    public record DepartmanOzet(Long id, String ad, String sehir) {
    }
}
