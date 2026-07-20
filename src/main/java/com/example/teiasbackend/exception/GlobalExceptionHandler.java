package com.example.teiasbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// 🌟 1. SİHİRLİ ETİKET: Bu etiket, sınıfımıza "Sistemdeki tüm Controller'ları izle, bir hata çıkarsa hemen araya gir!" emri verir.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🌟 2. SİHİRLİ ETİKET: Aşçımızın fırlattığı tüm RuntimeException'ları (çalışma zamanı hatalarını) yakala!
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {

        // Dış dünyaya göndereceğimiz şık tepsiyi (JSON objesini) hazırlıyoruz:
        Map<String, Object> hataCevabi = new HashMap<>();
        hataCevabi.put("zaman", LocalDateTime.now().toString()); // Hatayı aldığımız anın saati
        hataCevabi.put("durum", HttpStatus.BAD_REQUEST.value()); // HTTP 400 Kodu
        hataCevabi.put("mesaj", ex.getMessage());                // Aşçımızın yazdığı hata notu

        // Şık paketimizi 400 Bad Request etiketiyle dış dünyaya sunuyoruz:
        return new ResponseEntity<>(hataCevabi, HttpStatus.BAD_REQUEST);
    }
}