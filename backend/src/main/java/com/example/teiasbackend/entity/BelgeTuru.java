package com.example.teiasbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "belge_turleri")
@Getter
@Setter
@NoArgsConstructor
public class BelgeTuru {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Sabit kod (ISG, OKUL_KABUL, VELI_IZIN ...) */
    @Column(nullable = false, unique = true, length = 40)
    private String kod;

    @Column(nullable = false, length = 150)
    private String ad;

    @Column(columnDefinition = "TEXT")
    private String aciklama;

    @Column(nullable = false)
    private boolean zorunlu = true;

    @Column(nullable = false)
    private int maxBoyutMb = 5;

    @Column(nullable = false)
    private boolean aktif = true;
}
