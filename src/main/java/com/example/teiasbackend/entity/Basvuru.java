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

    @Column(nullable = false)
    private String durum = "BEKLEMEDE"; // Varsayılan olarak her başvuru beklemede başlar

    private LocalDateTime basvuruTarihi;

    // 🌟 İŞTE SİHİRLİ BAĞLANTI NOKTASI (@ManyToOne):
    // "Birçok başvuru, TEK BİR kullanıcıya ait olabilir."
    // Bu kod, veritabanındaki 'basvurular' tablosuna 'user_id' adında bir sütun (Foreign Key) ekler!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Her yeni başvuru veritabanına kaydedilirken, tarih otomatik olarak şu anki zaman olsun:
    @PrePersist
    public void otomatikTarihEkle() {
        this.basvuruTarihi = LocalDateTime.now();
    }
}