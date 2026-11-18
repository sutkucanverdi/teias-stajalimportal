package com.example.teiasbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "departmanlar")
@Getter
@Setter
@NoArgsConstructor
public class Departman {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String ad;

    @Column(columnDefinition = "TEXT")
    private String aciklama;

    @Column(nullable = false, length = 60)
    private String sehir;

    /** Dönem için alınacak toplam stajyer sayısı */
    @Column(nullable = false)
    private int kontenjan;

    @Column(nullable = false)
    private boolean aktif = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime olusturmaTarihi;
}
