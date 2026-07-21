package com.example.teiasbackend.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🌟 1. ÖZEL TUZAK: Veritabanında çift kayıt (unique kuralı ihlali) olduğunda kesinlikle burası çalışacak!
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {

        Map<String, Object> hataCevabi = new HashMap<>();
        hataCevabi.put("zaman", LocalDateTime.now().toString());
        hataCevabi.put("durum", HttpStatus.CONFLICT.value()); // HTTP 409 Conflict (Çakışma / Zaten Var) Kodu
        hataCevabi.put("mesaj", "HATA: Bu TC Kimlik numarası ile sistemde zaten kayıtlı bir kullanıcı bulunmaktadır!");

        return new ResponseEntity<>(hataCevabi, HttpStatus.CONFLICT);
    }

    // 🌟 2. GENEL TUZAK: Bizim TC algoritmamız gibi diğer çalışma zamanı hatalarını yakalar (400 Bad Request)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {

        Map<String, Object> hataCevabi = new HashMap<>();
        hataCevabi.put("zaman", LocalDateTime.now().toString());
        hataCevabi.put("durum", HttpStatus.BAD_REQUEST.value()); // HTTP 400 Kodu
        hataCevabi.put("mesaj", ex.getMessage());

        return new ResponseEntity<>(hataCevabi, HttpStatus.BAD_REQUEST);
    }
}