package com.example.teiasbackend.service;

import org.junit.jupiter.api.Test;

import static com.example.teiasbackend.entity.BasvuruDurumu.*;
import static com.example.teiasbackend.entity.Rol.*;
import static org.assertj.core.api.Assertions.assertThat;

class BasvuruIsAkisiTest {

    @Test
    void ogrenciYalnizcaTaslakVeyaEksikBelgeyiGonderebilir() {
        assertThat(BasvuruIsAkisi.gecisIzinliMi(TASLAK, GONDERILDI, ROLE_STUDENT)).isTrue();
        assertThat(BasvuruIsAkisi.gecisIzinliMi(EKSIK_BELGE, GONDERILDI, ROLE_STUDENT)).isTrue();
        assertThat(BasvuruIsAkisi.gecisIzinliMi(GONDERILDI, ONAYLANDI, ROLE_STUDENT)).isFalse();
        assertThat(BasvuruIsAkisi.izinliGecisler(IK_INCELEMESINDE, ROLE_STUDENT)).isEmpty();
    }

    @Test
    void ikBasvuruyuBirimeYonlendirebilirAmaTaslagaDokunamaz() {
        assertThat(BasvuruIsAkisi.gecisIzinliMi(GONDERILDI, IK_INCELEMESINDE, ROLE_HR)).isTrue();
        assertThat(BasvuruIsAkisi.gecisIzinliMi(IK_INCELEMESINDE, BIRIM_DEGERLENDIRMESINDE, ROLE_HR)).isTrue();
        assertThat(BasvuruIsAkisi.gecisIzinliMi(IK_INCELEMESINDE, EKSIK_BELGE, ROLE_HR)).isTrue();
        assertThat(BasvuruIsAkisi.izinliGecisler(TASLAK, ROLE_HR)).isEmpty();
    }

    @Test
    void birimYalnizcaKendiAsamasindaKararVerir() {
        assertThat(BasvuruIsAkisi.izinliGecisler(BIRIM_DEGERLENDIRMESINDE, ROLE_BIRIM))
                .containsExactlyInAnyOrder(ONAYLANDI, REDDEDILDI, IK_INCELEMESINDE);
        assertThat(BasvuruIsAkisi.gecisIzinliMi(IK_INCELEMESINDE, ONAYLANDI, ROLE_BIRIM)).isFalse();
        assertThat(BasvuruIsAkisi.gecisIzinliMi(GONDERILDI, BIRIM_DEGERLENDIRMESINDE, ROLE_BIRIM)).isFalse();
    }

    @Test
    void retVeEksikBelgeIcinAciklamaZorunlu() {
        assertThat(BasvuruIsAkisi.aciklamaZorunlu(IK_INCELEMESINDE, REDDEDILDI)).isTrue();
        assertThat(BasvuruIsAkisi.aciklamaZorunlu(GONDERILDI, EKSIK_BELGE)).isTrue();
        assertThat(BasvuruIsAkisi.aciklamaZorunlu(BIRIM_DEGERLENDIRMESINDE, IK_INCELEMESINDE)).isTrue();
        assertThat(BasvuruIsAkisi.aciklamaZorunlu(GONDERILDI, IK_INCELEMESINDE)).isFalse();
        assertThat(BasvuruIsAkisi.aciklamaZorunlu(BIRIM_DEGERLENDIRMESINDE, ONAYLANDI)).isFalse();
    }
}
