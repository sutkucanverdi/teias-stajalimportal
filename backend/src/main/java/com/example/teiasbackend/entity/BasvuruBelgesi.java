package com.example.teiasbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "basvuru_belgeleri")
@Getter
@Setter
@NoArgsConstructor
public class BasvuruBelgesi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "basvuru_id", nullable = false)
    private Basvuru basvuru;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "belge_turu_id", nullable = false)
    private BelgeTuru belgeTuru;

    /** Depolama dizinine göre göreli dosya adı */
    @Column(nullable = false, length = 500)
    private String dosyaYolu;

    @Column(nullable = false, length = 255)
    private String orijinalAd;

    @Column(length = 100)
    private String icerikTuru;

    @Column(nullable = false)
    private long boyut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BelgeDurumu durum = BelgeDurumu.BEKLIYOR;

    @Column(length = 500)
    private String yoneticiNotu;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime yuklemeTarihi;
}
