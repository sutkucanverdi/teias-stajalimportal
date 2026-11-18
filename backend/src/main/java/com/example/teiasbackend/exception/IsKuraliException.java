package com.example.teiasbackend.exception;

/** 400 - İş kuralı ihlali (ör. geçersiz durum geçişi, eksik belge) */
public class IsKuraliException extends RuntimeException {
    public IsKuraliException(String mesaj) {
        super(mesaj);
    }
}
