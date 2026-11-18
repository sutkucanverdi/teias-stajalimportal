package com.example.teiasbackend.service;

import com.example.teiasbackend.entity.BasvuruDurumu;
import com.example.teiasbackend.entity.Rol;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.teiasbackend.entity.BasvuruDurumu.*;

/**
 * Başvuru iş akışı (durum makinesi). Hangi rolün hangi durumdan hangi duruma geçirebileceğini tanımlar.
 *
 * <pre>
 *  TASLAK ──(öğrenci gönderir)──► GONDERILDI ──(İK)──► IK_INCELEMESINDE ──(İK)──► BIRIM_DEGERLENDIRMESINDE ──(Birim)──► ONAYLANDI
 *                                   ▲    │                  │                         │                      └─► REDDEDILDI
 *                                   │    └──► EKSIK_BELGE ◄─┘                         └─(Birim: İK'ya iade)─► IK_INCELEMESINDE
 *                                   └──(öğrenci yeniden gönderir)──┘
 * </pre>
 */
public final class BasvuruIsAkisi {

    private static final Map<BasvuruDurumu, Set<BasvuruDurumu>> OGRENCI = Map.of(
            TASLAK, EnumSet.of(GONDERILDI),
            EKSIK_BELGE, EnumSet.of(GONDERILDI)
    );

    private static final Map<BasvuruDurumu, Set<BasvuruDurumu>> IK = Map.of(
            GONDERILDI, EnumSet.of(IK_INCELEMESINDE, EKSIK_BELGE, BIRIM_DEGERLENDIRMESINDE, REDDEDILDI),
            IK_INCELEMESINDE, EnumSet.of(EKSIK_BELGE, BIRIM_DEGERLENDIRMESINDE, REDDEDILDI),
            EKSIK_BELGE, EnumSet.of(IK_INCELEMESINDE, REDDEDILDI),
            BIRIM_DEGERLENDIRMESINDE, EnumSet.of(IK_INCELEMESINDE, ONAYLANDI, REDDEDILDI),
            REDDEDILDI, EnumSet.of(IK_INCELEMESINDE),
            ONAYLANDI, EnumSet.of(IK_INCELEMESINDE)
    );

    private static final Map<BasvuruDurumu, Set<BasvuruDurumu>> BIRIM = Map.of(
            BIRIM_DEGERLENDIRMESINDE, EnumSet.of(ONAYLANDI, REDDEDILDI, IK_INCELEMESINDE)
    );

    private BasvuruIsAkisi() {
    }

    public static Set<BasvuruDurumu> izinliGecisler(BasvuruDurumu mevcut, Rol rol) {
        Map<BasvuruDurumu, Set<BasvuruDurumu>> tablo = switch (rol) {
            case ROLE_STUDENT -> OGRENCI;
            case ROLE_HR -> IK;
            case ROLE_BIRIM -> BIRIM;
        };
        return tablo.getOrDefault(mevcut, Set.of());
    }

    public static List<BasvuruDurumu> izinliGecislerSirali(BasvuruDurumu mevcut, Rol rol) {
        return izinliGecisler(mevcut, rol).stream().sorted().toList();
    }

    public static boolean gecisIzinliMi(BasvuruDurumu mevcut, BasvuruDurumu yeni, Rol rol) {
        return izinliGecisler(mevcut, rol).contains(yeni);
    }

    /** Bu geçiş için açıklama yazılması zorunlu mu? */
    public static boolean aciklamaZorunlu(BasvuruDurumu mevcut, BasvuruDurumu yeni) {
        return yeni == EKSIK_BELGE || yeni == REDDEDILDI
                || (mevcut == BIRIM_DEGERLENDIRMESINDE && yeni == IK_INCELEMESINDE)
                || (mevcut.sonuclandi() && yeni == IK_INCELEMESINDE);
    }
}
