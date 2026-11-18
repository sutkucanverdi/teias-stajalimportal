package com.example.teiasbackend.exception;

/** 409 - Benzersizlik çakışması (ör. aynı TC ile ikinci kayıt) */
public class CakismaException extends RuntimeException {
    public CakismaException(String mesaj) {
        super(mesaj);
    }
}
