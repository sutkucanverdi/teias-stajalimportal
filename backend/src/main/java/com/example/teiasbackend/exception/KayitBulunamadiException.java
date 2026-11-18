package com.example.teiasbackend.exception;

/** 404 - İstenen kayıt bulunamadı */
public class KayitBulunamadiException extends RuntimeException {
    public KayitBulunamadiException(String mesaj) {
        super(mesaj);
    }
}
