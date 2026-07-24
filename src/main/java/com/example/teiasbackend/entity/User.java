package com.example.teiasbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String adSoyad;

    @Column(unique = true)
    private String tcKimlik;

    @Column(unique = true)
    private String kullaniciAdi;

    private String sifre;

    private String rol;
}