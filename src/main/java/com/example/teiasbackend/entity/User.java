package com.example.teiasbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ad;
    private String soyad;

    @Column(unique = true)
    private String tcKimlik;

    @Column(unique = true)
    private String kullaniciAdi;

    private String email;
    private String telefon;
    private LocalDate dogumTarihi;

    private String sifre;
    private String rol;

    private boolean aktifMi = false;
    private String aktivasyonKodu;
}