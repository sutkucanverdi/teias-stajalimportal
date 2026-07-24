package com.example.teiasbackend.service;

import com.example.teiasbackend.dto.BasvuruCreateRequestDto;
import com.example.teiasbackend.dto.BasvuruResponseDto;
import com.example.teiasbackend.entity.Basvuru;
import com.example.teiasbackend.entity.User;
import com.example.teiasbackend.repository.BasvuruRepository;
import com.example.teiasbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.example.teiasbackend.dto.BasvuruDurumGuncelleRequestDto;

@Service
@RequiredArgsConstructor
public class BasvuruService {

    private final BasvuruRepository basvuruRepository;
    private final UserRepository userRepository;
    private String dosyaKaydet(MultipartFile dosya, String tur) {
        if (dosya == null || dosya.isEmpty()) return null;
        try {
            File uploadDir = new File("uploads/");
            if (!uploadDir.exists()) uploadDir.mkdirs();

            String dosyaAdi = System.currentTimeMillis() + "_" + tur + "_" + dosya.getOriginalFilename();
            Path dosyaYolu = Paths.get(uploadDir.getAbsolutePath() + "/" + dosyaAdi);
            Files.write(dosyaYolu, dosya.getBytes());

            return dosyaAdi;
        } catch (IOException e) {
            throw new RuntimeException("HATA: " + tur + " dosyası kaydedilemedi: " + e.getMessage());
        }
    }
    private BasvuruResponseDto mapToResponseDto(Basvuru b) {
        String adSoyad = (b.getUser() != null) ? b.getUser().getAdSoyad() : b.getAdSoyad();

        return BasvuruResponseDto.builder()
                .id(b.getId())
                .adSoyad(adSoyad)
                .tcKimlik(b.getTcKimlik())
                .eposta(b.getEposta())
                .telefon(b.getTelefon())
                .okulAdi(b.getOkulAdi())
                .alan(b.getAlan())
                .dal(b.getDal())
                .sinif(b.getSinif())
                .notOrtalamasi(b.getNotOrtalamasi())
                .stajTuru(b.getStajTuru())
                .departman(b.getDepartman())
                .sehir(b.getSehir())
                .baslamaTarihi(b.getBaslamaTarihi())
                .bitisTarihi(b.getBitisTarihi())
                .isgBelgesiYolu(b.getIsgBelgesiYolu())
                .okulKabulFormuYolu(b.getOkulKabulFormuYolu())
                .veliIzinBelgesiYolu(b.getVeliIzinBelgesiYolu())
                .durum(b.getDurum())
                .basvuruTarihi(b.getBasvuruTarihi())
                .build();
    }
    public BasvuruResponseDto basvuruOlustur(BasvuruCreateRequestDto requestDto,
                                             MultipartFile isg,
                                             MultipartFile okulFormu,
                                             MultipartFile veliIzni) {
        User stajyer = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("HATA: Sistemde " + requestDto.getUserId() + " ID numarasına sahip stajyer bulunamadı!"));
        Basvuru basvuru = new Basvuru();
        basvuru.setUser(stajyer);
        basvuru.setTcKimlik(requestDto.getTcKimlik());
        basvuru.setAdSoyad(requestDto.getAdSoyad() != null ? requestDto.getAdSoyad() : stajyer.getAdSoyad());
        basvuru.setEposta(requestDto.getEposta());
        basvuru.setTelefon(requestDto.getTelefon());
        basvuru.setOkulAdi(requestDto.getOkulAdi());
        basvuru.setAlan(requestDto.getAlan());
        basvuru.setDal(requestDto.getDal());
        basvuru.setSinif(requestDto.getSinif());
        basvuru.setNotOrtalamasi(requestDto.getNotOrtalamasi());
        basvuru.setStajTuru(requestDto.getStajTuru());
        basvuru.setDepartman(requestDto.getDepartman());
        basvuru.setSehir(requestDto.getSehir());
        basvuru.setBaslamaTarihi(requestDto.getBaslamaTarihi());
        basvuru.setBitisTarihi(requestDto.getBitisTarihi());
        basvuru.setIsgBelgesiYolu(dosyaKaydet(isg, "isg"));
        basvuru.setOkulKabulFormuYolu(dosyaKaydet(okulFormu, "okul"));
        basvuru.setVeliIzinBelgesiYolu(dosyaKaydet(veliIzni, "veli"));
        Basvuru kaydedilen = basvuruRepository.save(basvuru);
        return mapToResponseDto(kaydedilen);
    }
    public List<BasvuruResponseDto> tumBasvurulariGetir() {
        return basvuruRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    public BasvuruResponseDto basvuruDurumGuncelle(Long id, BasvuruDurumGuncelleRequestDto requestDto) {
        Basvuru basvuru = basvuruRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HATA: " + id + " numaralı başvuru sistemde bulunamadı!"));

        basvuru.setDurum(requestDto.getDurum());
        Basvuru guncelBasvuru = basvuruRepository.save(basvuru);

        return mapToResponseDto(guncelBasvuru);
    }

    public BasvuruResponseDto basvuruGuncelleVeDosyaYukle(Long id, String yeniDepartman, MultipartFile dosya) {
        Basvuru basvuru = basvuruRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HATA: " + id + " numaralı başvuru bulunamadı!"));

        if (yeniDepartman != null && !yeniDepartman.trim().isEmpty()) {
            basvuru.setDepartman(yeniDepartman);
        }

        if (dosya != null && !dosya.isEmpty()) {
            String dosyaAdi = dosyaKaydet(dosya, "ekdosya");
            basvuru.setOkulKabulFormuYolu(dosyaAdi);
        }

        Basvuru guncellenen = basvuruRepository.save(basvuru);
        return mapToResponseDto(guncellenen);
    }

    // 🌟 Duruma göre filtreleyip getiren metot:
    public List<BasvuruResponseDto> durumaGoreBasvurulariGetir(String durum) {
        return basvuruRepository.findByDurum(durum).stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    public List<BasvuruResponseDto> departmanaGoreBasvurulariGetir(String departman) {
        return basvuruRepository.findByDepartman(departman).stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    public String basvuruSil(Long id) {
        if (!basvuruRepository.existsById(id)) {
            throw new RuntimeException("HATA: " + id + " numaralı başvuru bulunamadı!");
        }
        basvuruRepository.deleteById(id);
        return id + " numaralı başvuru sistemden başarıyla silindi! 🗑️";
    }

    public List<BasvuruResponseDto> dinamikBasvuruFiltrele(String durum, String departman, String sehir) {
        return basvuruRepository.cokluFiltrele(durum, departman, sehir).stream()
                .map(this::mapToResponseDto)
                .toList();
    }
}