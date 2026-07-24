package com.example.teiasbackend.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "basvurular")
@Data
public class Basvuru {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tcKimlik;
    private String adSoyad;
    private String eposta;
    private String telefon;

    private String okulAdi;
    private String alan;
    private String dal;
    private String sinif;
    private Double notOrtalamasi;

    private String stajTuru;
    private String departman;
    private String sehir;
    private String baslamaTarihi;
    private String bitisTarihi;

    private String isgBelgesiYolu;
    private String okulKabulFormuYolu;
    private String veliIzinBelgesiYolu;

    private String durum = "İNCELENİYOR";
    private LocalDateTime basvuruTarihi = LocalDateTime.now();
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}