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
import com.example.teiasbackend.dto.BasvuruDurumGuncelleRequestDto;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.example.teiasbackend.dto.BasvuruGuncelleRequestDto;
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
    // 🌟 İK Yöneticisinin başvuru durumunu (ONAYLANDI/REDDEDİLDİ) güncellemesini sağlayan metod:
    public BasvuruResponseDto basvuruDurumGuncelle(Long id, BasvuruDurumGuncelleRequestDto requestDto) {
        // 1. Veritabanında bu ID'ye sahip bir başvuru var mı diye kontrol ediyoruz:
        Basvuru basvuru = basvuruRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HATA: " + id + " numaralı başvuru sistemde bulunamadı!"));

        // 2. Yeni durumu (Örn: "ONAYLANDI") eski durumun yerine yazıyoruz:
        basvuru.setDurum(requestDto.getDurum());

        // 3. Güncellenmiş haliyle veritabanına geri kaydediyoruz:
        Basvuru guncelBasvuru = basvuruRepository.save(basvuru);

        // 4. Güncel bilgileri dış dünyaya sunum tepsimiz (ResponseDto) ile geri gönderiyoruz:
        return BasvuruResponseDto.builder()
                .id(guncelBasvuru.getId())
                .adSoyad(guncelBasvuru.getUser().getAdSoyad()) // İlişkiden adı tekrar çektik!
                .departman(guncelBasvuru.getDepartman())
                .durum(guncelBasvuru.getDurum()) // İşte yeni onaylanan durum buraya geldi!
                .basvuruTarihi(guncelBasvuru.getBasvuruTarihi())
                .build();
    }
    // 🌟 Başvuru güncelleme ve dosya (PDF/CV) yükleme metodu:
    public BasvuruResponseDto basvuruGuncelleVeDosyaYukle(Long id, String yeniDepartman, MultipartFile dosya) {

        Basvuru basvuru = basvuruRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HATA: " + id + " numaralı başvuru bulunamadı!"));

        // Eğer yeni departman yazıldıysa güncelliyoruz:
        if (yeniDepartman != null && !yeniDepartman.trim().isEmpty()) {
            basvuru.setDepartman(yeniDepartman);
        }

        // Eğer dosya yüklendiyse kaydediyoruz:
        if (dosya != null && !dosya.isEmpty()) {
            try {
                String uploadKlasoru = "uploads/";
                File klasor = new File(uploadKlasoru);
                if (!klasor.exists()) {
                    klasor.mkdirs();
                }

                String dosyaAdi = System.currentTimeMillis() + "_" + dosya.getOriginalFilename();
                Path dosyaYolu = Paths.get(uploadKlasoru + dosyaAdi);
                Files.write(dosyaYolu, dosya.getBytes());

                basvuru.setBelgeYolu(dosyaYolu.toString());

            } catch (IOException e) {
                throw new RuntimeException("HATA: Dosya yüklenirken hata oluştu: " + e.getMessage());
            }
        }

        Basvuru guncellenen = basvuruRepository.save(basvuru);

        return BasvuruResponseDto.builder()
                .id(guncellenen.getId())
                .adSoyad(guncellenen.getUser().getAdSoyad())
                .departman(guncellenen.getDepartman())
                .durum(guncellenen.getDurum())
                .basvuruTarihi(guncellenen.getBasvuruTarihi())
                .build();
    }
    // 🌟 Duruma göre filtreleyip getiren metot (Örn: Sadece İNCELENİYOR olanlar):
    public List<BasvuruResponseDto> durumaGoreBasvurulariGetir(String durum) {
        return basvuruRepository.findByDurum(durum).stream()
                .map(b -> BasvuruResponseDto.builder()
                        .id(b.getId())
                        .adSoyad(b.getUser().getAdSoyad())
                        .departman(b.getDepartman())
                        .durum(b.getDurum())
                        .basvuruTarihi(b.getBasvuruTarihi())
                        .build())
                .toList();
    }

    // 🌟 Departmana göre filtreleyip getiren metot:
    public List<BasvuruResponseDto> departmanaGoreBasvurulariGetir(String departman) {
        return basvuruRepository.findByDepartman(departman).stream()
                .map(b -> BasvuruResponseDto.builder()
                        .id(b.getId())
                        .adSoyad(b.getUser().getAdSoyad())
                        .departman(b.getDepartman())
                        .durum(b.getDurum())
                        .basvuruTarihi(b.getBasvuruTarihi())
                        .build())
                .toList();
    }
    // 🌟 Başvuru silme / iptal etme metodu:
    public String basvuruSil(Long id) {
        // 1. Önce böyle bir başvuru gerçekten var mı diye kontrol ediyoruz:
        if (!basvuruRepository.existsById(id)) {
            throw new RuntimeException("HATA: " + id + " numaralı başvuru bulunamadı!");
        }

        // 2. Varsa veritabanından söküp atıyoruz:
        basvuruRepository.deleteById(id);

        return id + " numaralı başvuru sistemden başarıyla silindi! 🗑️";
    }
    // 🌟 Çoklu ve Dinamik Filtreleme Servisi:
    public List<BasvuruResponseDto> dinamikBasvuruFiltrele(String durum, String departman, String sehir) {
        return basvuruRepository.cokluFiltrele(durum, departman, sehir).stream()
                .map(b -> BasvuruResponseDto.builder()
                        .id(b.getId())
                        .adSoyad(b.getUser().getAdSoyad())
                        .departman(b.getDepartman())
                        .durum(b.getDurum())
                        .basvuruTarihi(b.getBasvuruTarihi())
                        .build())
                .toList();
    }
}