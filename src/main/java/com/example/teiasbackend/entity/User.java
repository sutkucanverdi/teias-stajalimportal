package com.example.teiasbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity                // Spring Boot'a "Bu sınıftan veritabanında bir TABLO yarat!" diyoruz.
@Table(name = "users") // Tablonun adı "users" olsun diyoruz.
@Data                  // Lombok, bizim için sıkıcı get/set kodlarını arkada otomatik yazıyor.
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Sistemin otomatik vereceği sıra no (1, 2, 3...)

    @Column(unique = true, nullable = false, length = 11)
    private String tcKimlik; // Eşsiz olmalı, boş olamaz, 11 karakter olmalı!

    @Column(nullable = false)
    private String adSoyad;

    @Column(nullable = false)
    private String sifre;

    private String rol; // Örn: "STUDENT", "ADMIN", "TEIAS_YETKILISI"
}