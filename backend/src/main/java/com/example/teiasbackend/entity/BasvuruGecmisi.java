package com.example.teiasbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** Başvurunun durum değişikliklerinin denetim kaydı (zaman çizelgesi) */
@Entity
@Table(name = "basvuru_gecmisi")
@Getter
@Setter
@NoArgsConstructor
public class BasvuruGecmisi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "basvuru_id", nullable = false)
    private Basvuru basvuru;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private BasvuruDurumu eskiDurum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BasvuruDurumu yeniDurum;

    @Column(columnDefinition = "TEXT")
    private String aciklama;

    @Column(nullable = false, length = 200)
    private String islemYapan;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime islemTarihi;
}
