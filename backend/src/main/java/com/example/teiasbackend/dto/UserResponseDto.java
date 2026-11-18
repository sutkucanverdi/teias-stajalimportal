package com.example.teiasbackend.dto;

import com.example.teiasbackend.entity.Rol;
import com.example.teiasbackend.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponseDto(
        Long id,
        String ad,
        String soyad,
        String tcKimlik,
        String kullaniciAdi,
        String email,
        String telefon,
        LocalDate dogumTarihi,
        Rol rol,
        boolean aktifMi,
        Long departmanId,
        String departmanAdi,
        LocalDateTime olusturmaTarihi
) {
    public static UserResponseDto from(User u) {
        return new UserResponseDto(
                u.getId(), u.getAd(), u.getSoyad(), u.getTcKimlik(), u.getKullaniciAdi(),
                u.getEmail(), u.getTelefon(), u.getDogumTarihi(), u.getRol(), u.isAktifMi(),
                u.getDepartman() != null ? u.getDepartman().getId() : null,
                u.getDepartman() != null ? u.getDepartman().getAd() : null,
                u.getOlusturmaTarihi()
        );
    }
}
