package com.example.teiasbackend.service;

import com.example.teiasbackend.dto.*;
import com.example.teiasbackend.entity.*;
import com.example.teiasbackend.exception.IsKuraliException;
import com.example.teiasbackend.exception.KayitBulunamadiException;
import com.example.teiasbackend.exception.YetkisizIslemException;
import com.example.teiasbackend.repository.*;
import com.example.teiasbackend.security.GirisYapanKullanici;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasvuruService {

    private final BasvuruRepository basvuruRepository;
    private final BasvuruBelgesiRepository belgeRepository;
    private final BasvuruGecmisiRepository gecmisRepository;
    private final BelgeTuruRepository belgeTuruRepository;
    private final DepartmanRepository departmanRepository;
    private final UserRepository userRepository;
    private final DosyaDepolamaService dosyaDepolamaService;

    // =====================================================================
    // Öğrenci işlemleri
    // =====================================================================

    @Transactional(readOnly = true)
    public List<BasvuruResponseDto> benimBasvurularim(GirisYapanKullanici k) {
        return basvuruRepository.findByUserIdOrderByOlusturmaTarihiDesc(k.id()).stream()
                .map(b -> detayaDonustur(b, k))
                .toList();
    }

    @Transactional
    public BasvuruResponseDto olustur(GirisYapanKullanici k, BasvuruCreateRequestDto dto) {
        if (basvuruRepository.existsByUserIdAndDurumNotIn(k.id(), EnumSet.of(BasvuruDurumu.REDDEDILDI))) {
            throw new IsKuraliException("Devam eden veya sonuçlanmış (onaylı) bir başvurunuz bulunuyor. Aynı anda yalnızca bir başvuru yapılabilir.");
        }
        User ogrenci = userRepository.findById(k.id())
                .orElseThrow(() -> new KayitBulunamadiException("Kullanıcı bulunamadı."));

        Basvuru b = new Basvuru();
        b.setUser(ogrenci);
        b.setDurum(BasvuruDurumu.TASLAK);
        bilgileriUygula(b, dto);
        basvuruRepository.save(b);
        gecmisEkle(b, null, BasvuruDurumu.TASLAK, "Başvuru taslağı oluşturuldu.", k);
        return detayaDonustur(b, k);
    }

    @Transactional
    public BasvuruResponseDto guncelle(GirisYapanKullanici k, Long id, BasvuruCreateRequestDto dto) {
        Basvuru b = bulDetayli(id);
        sahipKontrolu(b, k);
        if (!b.getDurum().ogrenciDuzenleyebilir()) {
            throw new IsKuraliException("Başvurunuz '" + b.getDurum().getEtiket() + "' durumunda olduğu için düzenlenemez.");
        }
        bilgileriUygula(b, dto);
        return detayaDonustur(b, k);
    }

    @Transactional
    public BasvuruResponseDto gonder(GirisYapanKullanici k, Long id) {
        Basvuru b = bulDetayli(id);
        sahipKontrolu(b, k);
        if (!BasvuruIsAkisi.gecisIzinliMi(b.getDurum(), BasvuruDurumu.GONDERILDI, Rol.ROLE_STUDENT)) {
            throw new IsKuraliException("Başvurunuz '" + b.getDurum().getEtiket() + "' durumunda olduğu için gönderilemez.");
        }
        if (b.getBaslamaTarihi().isBefore(LocalDate.now())) {
            throw new IsKuraliException("Staj başlama tarihi geçmiş bir tarih olamaz. Lütfen tarihleri güncelleyiniz.");
        }
        if (b.getTercihDepartman() == null || !b.getTercihDepartman().isAktif()) {
            throw new IsKuraliException("Tercih ettiğiniz birim artık başvuru kabul etmiyor. Lütfen başka bir birim seçiniz.");
        }

        List<String> eksikler = eksikZorunluBelgeler(b);
        if (!eksikler.isEmpty()) {
            throw new IsKuraliException("Başvuruyu göndermeden önce şu zorunlu belgeleri yüklemelisiniz: " + String.join(", ", eksikler));
        }

        BasvuruDurumu eski = b.getDurum();
        b.setDurum(BasvuruDurumu.GONDERILDI);
        if (b.getBasvuruTarihi() == null) {
            b.setBasvuruTarihi(LocalDateTime.now());
        }
        gecmisEkle(b, eski, BasvuruDurumu.GONDERILDI,
                eski == BasvuruDurumu.EKSIK_BELGE ? "Eksik belgeler tamamlanarak başvuru yeniden gönderildi." : "Başvuru İK'ya gönderildi.", k);
        return detayaDonustur(b, k);
    }

    @Transactional
    public MesajDto sil(GirisYapanKullanici k, Long id) {
        Basvuru b = bulDetayli(id);
        if (k.ogrenciMi()) {
            sahipKontrolu(b, k);
            if (b.getDurum() != BasvuruDurumu.TASLAK) {
                throw new IsKuraliException("Yalnızca taslak durumundaki başvurular silinebilir.");
            }
        } else if (!k.ikMi()) {
            throw new YetkisizIslemException("Başvuru silme yetkiniz bulunmamaktadır.");
        }
        b.getBelgeler().forEach(belge -> dosyaDepolamaService.sil(belge.getDosyaYolu()));
        basvuruRepository.delete(b);
        return new MesajDto(id + " numaralı başvuru sistemden başarıyla silindi.");
    }

    // =====================================================================
    // Ortak görüntüleme
    // =====================================================================

    @Transactional(readOnly = true)
    public BasvuruResponseDto getir(GirisYapanKullanici k, Long id) {
        Basvuru b = bulDetayli(id);
        erisimKontrolu(b, k);
        return detayaDonustur(b, k);
    }

    @Transactional(readOnly = true)
    public List<GecmisResponseDto> gecmis(GirisYapanKullanici k, Long id) {
        Basvuru b = bulDetayli(id);
        erisimKontrolu(b, k);
        return gecmisRepository.findByBasvuruIdOrderByIslemTarihiAscIdAsc(id).stream()
                .map(GecmisResponseDto::from).toList();
    }

    // =====================================================================
    // İK / Birim işlemleri
    // =====================================================================

    public record Filtre(BasvuruDurumu durum, Long departmanId, String sehir, StajTuru stajTuru, String arama) {
    }

    @Transactional(readOnly = true)
    public SayfaDto<BasvuruOzetDto> listele(GirisYapanKullanici k, Filtre filtre, Pageable pageable) {
        Page<Basvuru> sayfa = basvuruRepository.findAll(spesifikasyon(k, filtre), pageable);
        return SayfaDto.from(sayfa, BasvuruOzetDto::from);
    }

    @Transactional(readOnly = true)
    public String csvDisaAktar(GirisYapanKullanici k, Filtre filtre) {
        List<Basvuru> liste = basvuruRepository.findAll(spesifikasyon(k, filtre).and((root, q, cb) -> {
            q.orderBy(cb.desc(root.get("basvuruTarihi")));
            return null;
        }));
        DateTimeFormatter tarih = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter zaman = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        StringBuilder sb = new StringBuilder("﻿"); // Excel'in Türkçe karakterleri doğru göstermesi için BOM
        sb.append("Başvuru No;Ad Soyad;TC Kimlik;E-posta;Telefon;Okul;Alan;Dal;Sınıf;Not Ort.;Staj Türü;Şehir;Tercih Birim;Atanan Birim;Başlama;Bitiş;Durum;Başvuru Tarihi\n");
        for (Basvuru b : liste) {
            sb.append(String.join(";",
                    String.valueOf(b.getId()),
                    csv(b.getUser().getAdSoyad()),
                    csv(b.getUser().getTcKimlik()),
                    csv(b.getUser().getEmail()),
                    csv(b.getUser().getTelefon()),
                    csv(b.getOkulAdi()),
                    csv(b.getAlan()),
                    csv(b.getDal()),
                    csv(b.getSinif()),
                    b.getNotOrtalamasi() == null ? "" : String.valueOf(b.getNotOrtalamasi()),
                    b.getStajTuru().name(),
                    csv(b.getSehir()),
                    csv(b.getTercihDepartman() != null ? b.getTercihDepartman().getAd() : ""),
                    csv(b.getAtananDepartman() != null ? b.getAtananDepartman().getAd() : ""),
                    b.getBaslamaTarihi().format(tarih),
                    b.getBitisTarihi().format(tarih),
                    b.getDurum().getEtiket(),
                    b.getBasvuruTarihi() == null ? "" : b.getBasvuruTarihi().format(zaman)
            )).append('\n');
        }
        return sb.toString();
    }

    @Transactional
    public BasvuruResponseDto durumGuncelle(GirisYapanKullanici k, Long id, BasvuruDurumGuncelleRequestDto dto) {
        Basvuru b = bulDetayli(id);
        erisimKontrolu(b, k);
        if (k.ogrenciMi()) {
            throw new YetkisizIslemException("Öğrenciler başvuru durumunu değiştiremez. Başvurunuzu göndermek için 'Gönder' işlemini kullanınız.");
        }

        BasvuruDurumu eski = b.getDurum();
        BasvuruDurumu yeni = dto.durum();
        if (!BasvuruIsAkisi.gecisIzinliMi(eski, yeni, k.rol())) {
            throw new IsKuraliException("'" + eski.getEtiket() + "' durumundaki başvuru '" + yeni.getEtiket() + "' durumuna alınamaz.");
        }
        String aciklama = dto.aciklama() == null ? null : dto.aciklama().trim();
        if (BasvuruIsAkisi.aciklamaZorunlu(eski, yeni) && (aciklama == null || aciklama.isEmpty())) {
            throw new IsKuraliException("Bu işlem için açıklama yazılması zorunludur.");
        }

        switch (yeni) {
            case BIRIM_DEGERLENDIRMESINDE -> {
                Departman hedef = dto.departmanId() != null
                        ? departmanRepository.findById(dto.departmanId()).orElseThrow(() -> new KayitBulunamadiException("Birim bulunamadı."))
                        : b.getTercihDepartman();
                if (hedef == null) {
                    throw new IsKuraliException("Yönlendirilecek birim seçilmelidir.");
                }
                if (!hedef.isAktif()) {
                    throw new IsKuraliException("Pasif durumdaki bir birime yönlendirme yapılamaz.");
                }
                b.setAtananDepartman(hedef);
                if (aciklama == null || aciklama.isEmpty()) {
                    aciklama = "Başvuru değerlendirme için " + hedef.getAd() + " birimine yönlendirildi.";
                }
            }
            case ONAYLANDI -> {
                Departman d = b.getAtananDepartman();
                if (d == null) {
                    throw new IsKuraliException("Onay için başvurunun bir birime atanmış olması gerekir.");
                }
                long onaylanan = basvuruRepository.countByAtananDepartmanIdAndDurum(d.getId(), BasvuruDurumu.ONAYLANDI);
                if (onaylanan >= d.getKontenjan()) {
                    throw new IsKuraliException(d.getAd() + " biriminin stajyer kontenjanı dolmuştur (" + d.getKontenjan() + ").");
                }
                if (aciklama == null || aciklama.isEmpty()) {
                    aciklama = "Staj başvurunuz kabul edilmiştir. Staj başlangıç tarihinde " + d.getAd() + " birimine başvurunuz rica olunur.";
                }
            }
            case IK_INCELEMESINDE -> {
                if (eski == BasvuruDurumu.BIRIM_DEGERLENDIRMESINDE || eski.sonuclandi()) {
                    b.setAtananDepartman(null);
                }
                if (aciklama == null || aciklama.isEmpty()) {
                    aciklama = "Başvuru İK tarafından incelemeye alındı.";
                }
            }
            default -> {
            }
        }

        b.setDurum(yeni);
        b.setDegerlendirmeNotu(aciklama);
        gecmisEkle(b, eski, yeni, aciklama, k);
        return detayaDonustur(b, k);
    }

    // =====================================================================
    // Yardımcılar
    // =====================================================================

    Basvuru bulDetayli(Long id) {
        return basvuruRepository.findWithDetayById(id)
                .orElseThrow(() -> new KayitBulunamadiException(id + " numaralı başvuru sistemde bulunamadı."));
    }

    /** Öğrenci yalnızca kendi başvurusunu, birim yalnızca kendisine atanmış başvuruyu görebilir. İK hepsini görür. */
    void erisimKontrolu(Basvuru b, GirisYapanKullanici k) {
        switch (k.rol()) {
            case ROLE_HR -> {
                if (b.getDurum() == BasvuruDurumu.TASLAK) {
                    throw new YetkisizIslemException("Taslak başvurular yalnızca öğrencinin kendisi tarafından görüntülenebilir.");
                }
            }
            case ROLE_STUDENT -> sahipKontrolu(b, k);
            case ROLE_BIRIM -> {
                if (b.getAtananDepartman() == null || !b.getAtananDepartman().getId().equals(k.departmanId())) {
                    throw new YetkisizIslemException("Bu başvuru biriminize yönlendirilmemiştir.");
                }
            }
        }
    }

    void sahipKontrolu(Basvuru b, GirisYapanKullanici k) {
        if (!b.getUser().getId().equals(k.id())) {
            throw new YetkisizIslemException("Bu başvuru üzerinde işlem yetkiniz bulunmamaktadır.");
        }
    }

    List<String> eksikZorunluBelgeler(Basvuru b) {
        Set<Long> yuklenenTurler = b.getBelgeler().stream()
                .filter(x -> x.getDurum() != BelgeDurumu.REDDEDILDI)
                .map(x -> x.getBelgeTuru().getId())
                .collect(Collectors.toSet());
        return belgeTuruRepository.findByAktifTrueAndZorunluTrue().stream()
                .filter(t -> !yuklenenTurler.contains(t.getId()))
                .map(BelgeTuru::getAd)
                .toList();
    }

    void gecmisEkle(Basvuru b, BasvuruDurumu eski, BasvuruDurumu yeni, String aciklama, GirisYapanKullanici k) {
        BasvuruGecmisi g = new BasvuruGecmisi();
        g.setBasvuru(b);
        g.setEskiDurum(eski);
        g.setYeniDurum(yeni);
        g.setAciklama(aciklama);
        g.setIslemYapan(k.adSoyad() + " (" + rolEtiketi(k.rol()) + ")");
        gecmisRepository.save(g);
    }

    private void bilgileriUygula(Basvuru b, BasvuruCreateRequestDto dto) {
        Departman tercih = departmanRepository.findById(dto.tercihDepartmanId())
                .orElseThrow(() -> new KayitBulunamadiException("Tercih edilen birim bulunamadı."));
        if (!tercih.isAktif()) {
            throw new IsKuraliException("Seçilen birim şu anda başvuru kabul etmemektedir.");
        }
        b.setOkulAdi(dto.okulAdi().trim());
        b.setAlan(dto.alan().trim());
        b.setDal(dto.dal() == null ? null : dto.dal().trim());
        b.setSinif(dto.sinif());
        b.setNotOrtalamasi(dto.notOrtalamasi());
        b.setStajTuru(dto.stajTuru());
        b.setSehir(dto.sehir().trim());
        b.setTercihDepartman(tercih);
        b.setBaslamaTarihi(dto.baslamaTarihi());
        b.setBitisTarihi(dto.bitisTarihi());
        b.setOgrenciNotu(dto.ogrenciNotu());
    }

    BasvuruResponseDto detayaDonustur(Basvuru b, GirisYapanKullanici k) {
        User u = b.getUser();
        List<BelgeResponseDto> belgeler = belgeRepository.findByBasvuruIdOrderByYuklemeTarihiAsc(b.getId()).stream()
                .map(BelgeResponseDto::from).toList();
        List<BasvuruDurumu> gecisler = izinliGecislerFor(b, k);
        boolean duzenlenebilir = k.ogrenciMi() && b.getDurum().ogrenciDuzenleyebilir();
        return new BasvuruResponseDto(
                b.getId(),
                new BasvuruResponseDto.OgrenciBilgisi(u.getId(), u.getAd(), u.getSoyad(), u.getTcKimlik(), u.getEmail(), u.getTelefon(), u.getDogumTarihi()),
                b.getOkulAdi(), b.getAlan(), b.getDal(), b.getSinif(), b.getNotOrtalamasi(), b.getStajTuru(), b.getSehir(),
                b.getBaslamaTarihi(), b.getBitisTarihi(), b.getOgrenciNotu(),
                departmanOzet(b.getTercihDepartman()), departmanOzet(b.getAtananDepartman()),
                b.getDurum(), b.getDurum().getEtiket(), b.getDegerlendirmeNotu(),
                b.getBasvuruTarihi(), b.getOlusturmaTarihi(), b.getGuncellemeTarihi(),
                belgeler, gecisler, duzenlenebilir
        );
    }

    private List<BasvuruDurumu> izinliGecislerFor(Basvuru b, GirisYapanKullanici k) {
        if (k.birimMi() && (b.getAtananDepartman() == null || !b.getAtananDepartman().getId().equals(k.departmanId()))) {
            return List.of();
        }
        return BasvuruIsAkisi.izinliGecislerSirali(b.getDurum(), k.rol());
    }

    private static BasvuruResponseDto.DepartmanOzet departmanOzet(Departman d) {
        return d == null ? null : new BasvuruResponseDto.DepartmanOzet(d.getId(), d.getAd(), d.getSehir());
    }

    private Specification<Basvuru> spesifikasyon(GirisYapanKullanici k, Filtre f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (k.ikMi()) {
                p.add(cb.notEqual(root.get("durum"), BasvuruDurumu.TASLAK));
            } else if (k.birimMi()) {
                p.add(cb.equal(root.get("atananDepartman").get("id"), k.departmanId()));
            } else {
                p.add(cb.equal(root.get("user").get("id"), k.id()));
            }
            if (f.durum() != null) {
                p.add(cb.equal(root.get("durum"), f.durum()));
            }
            if (f.departmanId() != null) {
                p.add(cb.or(
                        cb.equal(root.get("atananDepartman").get("id"), f.departmanId()),
                        cb.and(cb.isNull(root.get("atananDepartman")), cb.equal(root.get("tercihDepartman").get("id"), f.departmanId()))
                ));
            }
            if (f.sehir() != null && !f.sehir().isBlank()) {
                p.add(cb.equal(cb.lower(root.get("sehir")), f.sehir().trim().toLowerCase(Locale.forLanguageTag("tr"))));
            }
            if (f.stajTuru() != null) {
                p.add(cb.equal(root.get("stajTuru"), f.stajTuru()));
            }
            if (f.arama() != null && !f.arama().isBlank()) {
                String q = "%" + f.arama().trim().toLowerCase(Locale.forLanguageTag("tr")) + "%";
                Join<Basvuru, User> user = root.join("user", JoinType.INNER);
                p.add(cb.or(
                        cb.like(cb.lower(cb.concat(cb.concat(user.get("ad"), " "), user.get("soyad"))), q),
                        cb.like(user.get("tcKimlik"), q),
                        cb.like(cb.lower(root.get("okulAdi")), q),
                        cb.like(cb.lower(root.get("alan")), q)
                ));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    static Sort varsayilanSiralama() {
        return Sort.by(Sort.Direction.DESC, "guncellemeTarihi");
    }

    private static String csv(String s) {
        if (s == null) return "";
        String temiz = s.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ");
        // CSV/Excel formül enjeksiyonuna karşı koruma
        if (!temiz.isEmpty() && "=+-@".indexOf(temiz.charAt(0)) >= 0) {
            temiz = "'" + temiz;
        }
        return temiz.contains(";") ? "\"" + temiz + "\"" : temiz;
    }

    static String rolEtiketi(Rol rol) {
        return switch (rol) {
            case ROLE_STUDENT -> "Öğrenci";
            case ROLE_HR -> "İnsan Kaynakları";
            case ROLE_BIRIM -> "Birim Sorumlusu";
        };
    }
}
