package com.example.teiasbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "basvurular")
@Getter
@Setter
@NoArgsConstructor
public class Basvuru {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Başvuruyu yapan öğrenci */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String okulAdi;

    @Column(nullable = false, length = 120)
    private String alan;

    @Column(length = 120)
    private String dal;

    @Column(nullable = false, length = 10)
    private String sinif;

    private Double notOrtalamasi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StajTuru stajTuru;

    @Column(nullable = false, length = 60)
    private String sehir;

    @Column(nullable = false)
    private LocalDate baslamaTarihi;

    @Column(nullable = false)
    private LocalDate bitisTarihi;

    @Column(columnDefinition = "TEXT")
    private String ogrenciNotu;

    /** Öğrencinin tercih ettiği birim */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tercih_departman_id")
    private Departman tercihDepartman;

    /** İK tarafından değerlendirme için yönlendirilen birim */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atanan_departman_id")
    private Departman atananDepartman;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BasvuruDurumu durum = BasvuruDurumu.TASLAK;

    /** Ret / eksik belge / değerlendirme açıklaması */
    @Column(columnDefinition = "TEXT")
    private String degerlendirmeNotu;

    /** Öğrencinin "Gönder" butonuna ilk bastığı an */
    private LocalDateTime basvuruTarihi;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime olusturmaTarihi;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime guncellemeTarihi;

    @OneToMany(mappedBy = "basvuru", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("yuklemeTarihi ASC")
    private List<BasvuruBelgesi> belgeler = new ArrayList<>();
}
