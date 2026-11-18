package com.example.teiasbackend.entity;

public enum BasvuruDurumu {
    TASLAK("Taslak"),
    GONDERILDI("Gönderildi"),
    IK_INCELEMESINDE("İK İncelemesinde"),
    EKSIK_BELGE("Eksik Belge"),
    BIRIM_DEGERLENDIRMESINDE("Birim Değerlendirmesinde"),
    ONAYLANDI("Onaylandı"),
    REDDEDILDI("Reddedildi");

    private final String etiket;

    BasvuruDurumu(String etiket) {
        this.etiket = etiket;
    }

    public String getEtiket() {
        return etiket;
    }

    /** Öğrencinin başvuruyu düzenleyebildiği / belge yükleyebildiği durumlar */
    public boolean ogrenciDuzenleyebilir() {
        return this == TASLAK || this == EKSIK_BELGE;
    }

    /** Sonuçlanmış (kapanmış) başvuru mu? */
    public boolean sonuclandi() {
        return this == ONAYLANDI || this == REDDEDILDI;
    }
}
