package com.example.teiasbackend.service;

import com.example.teiasbackend.dto.BelgeDurumGuncelleRequestDto;
import com.example.teiasbackend.dto.BelgeResponseDto;
import com.example.teiasbackend.dto.MesajDto;
import com.example.teiasbackend.entity.*;
import com.example.teiasbackend.exception.IsKuraliException;
import com.example.teiasbackend.exception.KayitBulunamadiException;
import com.example.teiasbackend.exception.YetkisizIslemException;
import com.example.teiasbackend.repository.BasvuruBelgesiRepository;
import com.example.teiasbackend.repository.BelgeTuruRepository;
import com.example.teiasbackend.security.GirisYapanKullanici;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BelgeService {

    private final BasvuruService basvuruService;
    private final BasvuruBelgesiRepository belgeRepository;
    private final BelgeTuruRepository belgeTuruRepository;
    private final DosyaDepolamaService dosyaDepolamaService;

    /** Öğrenci belge yükler. Aynı türde onaylanmamış bir belge varsa yenisiyle değiştirilir. */
    @Transactional
    public BelgeResponseDto yukle(GirisYapanKullanici k, Long basvuruId, Long belgeTuruId, MultipartFile dosya) {
        Basvuru b = basvuruService.bulDetayli(basvuruId);
        basvuruService.sahipKontrolu(b, k);
        if (!b.getDurum().ogrenciDuzenleyebilir()) {
            throw new IsKuraliException("Başvurunuz '" + b.getDurum().getEtiket() + "' durumunda olduğu için belge yüklenemez.");
        }
        BelgeTuru tur = belgeTuruRepository.findById(belgeTuruId)
                .filter(BelgeTuru::isAktif)
                .orElseThrow(() -> new KayitBulunamadiException("Geçersiz veya pasif belge türü."));

        List<BasvuruBelgesi> ayniTur = b.getBelgeler().stream()
                .filter(x -> x.getBelgeTuru().getId().equals(tur.getId()))
                .toList();
        if (ayniTur.stream().anyMatch(x -> x.getDurum() == BelgeDurumu.ONAYLANDI)) {
            throw new IsKuraliException("'" + tur.getAd() + "' belgeniz İK tarafından onaylanmış olduğu için değiştirilemez.");
        }

        DosyaDepolamaService.KayitliDosya kayit = dosyaDepolamaService.kaydet(dosya, "basvuru-" + b.getId(), tur.getMaxBoyutMb());

        // Eski (bekleyen / reddedilmiş) belgeyi kaldır
        for (BasvuruBelgesi eski : ayniTur) {
            dosyaDepolamaService.sil(eski.getDosyaYolu());
            b.getBelgeler().remove(eski);
        }

        BasvuruBelgesi yeni = new BasvuruBelgesi();
        yeni.setBasvuru(b);
        yeni.setBelgeTuru(tur);
        yeni.setDosyaYolu(kayit.goreliYol());
        yeni.setOrijinalAd(orijinalAdTemizle(dosya.getOriginalFilename()));
        yeni.setIcerikTuru(kayit.icerikTuru());
        yeni.setBoyut(kayit.boyut());
        yeni.setDurum(BelgeDurumu.BEKLIYOR);
        b.getBelgeler().add(yeni);
        belgeRepository.save(yeni);
        return BelgeResponseDto.from(yeni);
    }

    @Transactional
    public MesajDto sil(GirisYapanKullanici k, Long basvuruId, Long belgeId) {
        Basvuru b = basvuruService.bulDetayli(basvuruId);
        basvuruService.sahipKontrolu(b, k);
        if (!b.getDurum().ogrenciDuzenleyebilir()) {
            throw new IsKuraliException("Bu aşamada belge silinemez.");
        }
        BasvuruBelgesi belge = b.getBelgeler().stream().filter(x -> x.getId().equals(belgeId)).findFirst()
                .orElseThrow(() -> new KayitBulunamadiException("Belge bulunamadı."));
        if (belge.getDurum() == BelgeDurumu.ONAYLANDI) {
            throw new IsKuraliException("Onaylanmış belgeler silinemez.");
        }
        dosyaDepolamaService.sil(belge.getDosyaYolu());
        b.getBelgeler().remove(belge);
        return new MesajDto("Belge silindi.");
    }

    public record IndirilecekBelge(Resource kaynak, String ad, String icerikTuru) {
    }

    @Transactional(readOnly = true)
    public IndirilecekBelge indir(GirisYapanKullanici k, Long belgeId) {
        BasvuruBelgesi belge = belgeRepository.findWithDetayById(belgeId)
                .orElseThrow(() -> new KayitBulunamadiException("Belge bulunamadı."));
        Basvuru b = belge.getBasvuru();
        if (k.ogrenciMi()) {
            basvuruService.sahipKontrolu(b, k);
        } else {
            basvuruService.erisimKontrolu(b, k);
        }
        return new IndirilecekBelge(dosyaDepolamaService.oku(belge.getDosyaYolu()), belge.getOrijinalAd(), belge.getIcerikTuru());
    }

    /** İK belgeyi onaylar / reddeder. */
    @Transactional
    public BelgeResponseDto durumGuncelle(GirisYapanKullanici k, Long belgeId, BelgeDurumGuncelleRequestDto dto) {
        if (!k.ikMi()) {
            throw new YetkisizIslemException("Belge değerlendirme yetkisi yalnızca İnsan Kaynakları'ndadır.");
        }
        BasvuruBelgesi belge = belgeRepository.findWithDetayById(belgeId)
                .orElseThrow(() -> new KayitBulunamadiException("Belge bulunamadı."));
        Basvuru b = belge.getBasvuru();
        if (b.getDurum() == BasvuruDurumu.TASLAK || b.getDurum().sonuclandi()) {
            throw new IsKuraliException("Bu aşamadaki başvurunun belgeleri değerlendirilemez.");
        }
        String not = dto.not() == null ? null : dto.not().trim();
        if (dto.durum() == BelgeDurumu.REDDEDILDI && (not == null || not.isEmpty())) {
            throw new IsKuraliException("Belgeyi reddederken öğrenciye iletilecek bir açıklama yazmalısınız.");
        }
        belge.setDurum(dto.durum());
        belge.setYoneticiNotu(not);
        return BelgeResponseDto.from(belge);
    }

    private static String orijinalAdTemizle(String ad) {
        if (ad == null || ad.isBlank()) return "belge";
        String sadeAd = ad.replace('\\', '/');
        sadeAd = sadeAd.substring(sadeAd.lastIndexOf('/') + 1);
        sadeAd = sadeAd.replaceAll("[\\p{Cntrl}\"]", "");
        return sadeAd.length() > 200 ? sadeAd.substring(sadeAd.length() - 200) : sadeAd;
    }
}
