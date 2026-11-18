package com.example.teiasbackend.dto;

import com.example.teiasbackend.entity.BasvuruDurumu;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Başvuru durumu değiştirme isteği.
 * <ul>
 *     <li>EKSIK_BELGE ve REDDEDILDI için {@code aciklama} zorunludur.</li>
 *     <li>BIRIM_DEGERLENDIRMESINDE için {@code departmanId} verilmezse öğrencinin tercih ettiği birim kullanılır.</li>
 * </ul>
 */
public record BasvuruDurumGuncelleRequestDto(
        @NotNull(message = "Yeni durum zorunludur.") BasvuruDurumu durum,
        @Size(max = 2000) String aciklama,
        Long departmanId
) {
}
