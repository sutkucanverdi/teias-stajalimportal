package com.example.teiasbackend.entity;

/** Sistemdeki kullanıcı rolleri. Spring Security yetkisi olarak doğrudan enum adı kullanılır. */
public enum Rol {
    /** Staj başvurusu yapan lise öğrencisi */
    ROLE_STUDENT,
    /** İnsan Kaynakları yöneticisi: tüm başvuruları yönetir, birimlere yönlendirir */
    ROLE_HR,
    /** Birim (alıcı departman) sorumlusu: kendisine yönlendirilen başvuruları değerlendirir */
    ROLE_BIRIM
}
