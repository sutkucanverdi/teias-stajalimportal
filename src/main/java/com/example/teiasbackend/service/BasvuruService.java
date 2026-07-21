package com.example.teiasbackend.service;

import com.example.teiasbackend.dto.BasvuruCreateRequestDto;
import com.example.teiasbackend.dto.BasvuruResponseDto;
import com.example.teiasbackend.entity.Basvuru;
import com.example.teiasbackend.entity.User;
import com.example.teiasbackend.repository.BasvuruRepository;
import com.example.teiasbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BasvuruService {

    // 🌟 AŞÇIMIZIN İKİ KİLER SORUMLUSUNA DA İHTİYACI VAR:
    // Hem başvuruları kaydetmek için BasvuruRepository'ye,
    // hem de "Bu ID'li öğrenci kim?" diye bakmak için UserRepository'ye bağlıyoruz!
    private final BasvuruRepository basvuruRepository;
    private final UserRepository userRepository;

    public BasvuruResponseDto basvuruOlustur(BasvuruCreateRequestDto requestDto) {

        // 1. Önce kullanıcının gönderdiği ID sistemde var mı diye kontrol ediyoruz!
        User stajyer = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("HATA: Sistemde " + requestDto.getUserId() + " ID numarasına sahip bir stajyer bulunamadı!"));

        // 2. Eğer öğrenci varsa yeni bir Başvuru nesnesi (Entity) oluşturup içini dolduruyoruz:
        Basvuru basvuru = new Basvuru();
        basvuru.setUser(stajyer); // 🌟 İŞTE İLİŞKİYİ KURDUĞUMUZ YER! Bu başvuru bu stajyere ait diyoruz.
        basvuru.setDepartman(requestDto.getDepartman());
        // Not: durum ("BEKLEMEDE") ve basvuruTarihi (@PrePersist sayesinde) otomatik atanacak!

        // 3. Veritabanına kaydediyoruz:
        Basvuru kaydedilenBasvuru = basvuruRepository.save(basvuru);

        // 4. Dış dünyaya (Frontend/Swagger) göndereceğimiz şık tepsiyi (ResponseDto) hazırlıyoruz:
        return BasvuruResponseDto.builder()
                .id(kaydedilenBasvuru.getId())
                .adSoyad(stajyer.getAdSoyad()) // İşte ilişkisel veritabanının gücü! User tablosundan adını çekip tepsimize koyduk!
                .departman(kaydedilenBasvuru.getDepartman())
                .durum(kaydedilenBasvuru.getDurum())
                .basvuruTarihi(kaydedilenBasvuru.getBasvuruTarihi())
                .build();
    }
    // 🌟 İK Yöneticisi için tüm başvuruları listeleyen metodumuz:
    public List<BasvuruResponseDto> tumBasvurulariGetir() {
        // 1. Kilerimizden (Veritabanından) tüm başvuru kayıtlarını çekiyoruz:
        List<Basvuru> basvurular = basvuruRepository.findAll();

        // 2. Java Streams (Akış) gücünü kullanarak her bir Entity'yi şık sunum tepsimize (DTO) dönüştürüyoruz:
        return basvurular.stream()
                .map(basvuru -> BasvuruResponseDto.builder()
                        .id(basvuru.getId())
                        .adSoyad(basvuru.getUser().getAdSoyad()) // İlişkiden stajyerin adını çektik!
                        .departman(basvuru.getDepartman())
                        .durum(basvuru.getDurum())
                        .basvuruTarihi(basvuru.getBasvuruTarihi())
                        .build())
                .toList(); // Listeye çevirip garsona teslim ediyoruz.
    }
}