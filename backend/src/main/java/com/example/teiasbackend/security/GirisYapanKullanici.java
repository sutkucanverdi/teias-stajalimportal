package com.example.teiasbackend.security;

import com.example.teiasbackend.entity.Rol;
import com.example.teiasbackend.entity.User;

/** SecurityContext içinde tutulan, istek boyunca kullanılan hafif kullanıcı bilgisi. */
public record GirisYapanKullanici(
        Long id,
        String kullaniciAdi,
        String adSoyad,
        Rol rol,
        Long departmanId
) {
    public static GirisYapanKullanici from(User u) {
        return new GirisYapanKullanici(
                u.getId(),
                u.getKullaniciAdi(),
                u.getAdSoyad(),
                u.getRol(),
                u.getDepartman() != null ? u.getDepartman().getId() : null
        );
    }

    public boolean ogrenciMi() {
        return rol == Rol.ROLE_STUDENT;
    }

    public boolean ikMi() {
        return rol == Rol.ROLE_HR;
    }

    public boolean birimMi() {
        return rol == Rol.ROLE_BIRIM;
    }
}
