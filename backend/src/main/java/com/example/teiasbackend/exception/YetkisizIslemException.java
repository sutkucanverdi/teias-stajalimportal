package com.example.teiasbackend.exception;

/** 403 - Kullanıcının bu kayıt üzerinde işlem yetkisi yok */
public class YetkisizIslemException extends RuntimeException {
    public YetkisizIslemException(String mesaj) {
        super(mesaj);
    }
}
