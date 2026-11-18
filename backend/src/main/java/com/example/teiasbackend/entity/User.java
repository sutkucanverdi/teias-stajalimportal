package com.example.teiasbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String ad;

    @Column(nullable = false, length = 100)
    private String soyad;

    @Column(unique = true, length = 11)
    private String tcKimlik;

    /** Öğrenciler için TC Kimlik No, personel için kurum kullanıcı adı */
    @Column(nullable = false, unique = true, length = 100)
    private String kullaniciAdi;

    @Column(unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String telefon;

    private LocalDate dogumTarihi;

    /** BCrypt ile şifrelenmiş parola */
    @Column(nullable = false, length = 100)
    private String sifre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Rol rol;

    @Column(nullable = false)
    private boolean aktifMi = true;

    @Column(length = 10)
    private String aktivasyonKodu;

    /** Yalnızca ROLE_BIRIM kullanıcıları için: sorumlu olduğu birim */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departman_id")
    private Departman departman;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime olusturmaTarihi;

    public String getAdSoyad() {
        return ad + " " + soyad;
    }
}
