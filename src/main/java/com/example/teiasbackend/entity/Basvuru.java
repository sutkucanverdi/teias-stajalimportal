package com.example.teiasbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "basvurular")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Basvuru {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String departman; // Örn: "BT", "İnsan Kaynakları"

    // 🌟 YENİ EKLENEN ALAN: Stajyerin başvurduğu şehir (Örn: "Ankara", "İstanbul")
    private String sehir = "Ankara"; // Varsayılan olarak Ankara atayabiliriz veya kullanıcıdan alabiliriz

    @Column(nullable = false)
    private String durum = "BEKLEMEDE"; // Varsayılan olarak her başvuru beklemede başlar

    private LocalDateTime basvuruTarihi;

    private String belgeYolu; // Stajyerin yükleyeceği belgenin dosya yolu

    // 🌟 İŞTE SİHİRLİ BAĞLANTI NOKTASI (@ManyToOne):
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Her yeni başvuru veritabanına kaydedilirken tarih otomatik atanır:
    @PrePersist
    public void otomatikTarihEkle() {
        this.basvuruTarihi = LocalDateTime.now();
    }
}