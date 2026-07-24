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
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {

        Map<String, Object> hataCevabi = new HashMap<>();
        hataCevabi.put("zaman", LocalDateTime.now().toString());
        hataCevabi.put("durum", HttpStatus.CONFLICT.value());
        hataCevabi.put("mesaj", "HATA: Bu TC Kimlik numarası ile sistemde zaten kayıtlı bir kullanıcı bulunmaktadır!");

        return new ResponseEntity<>(hataCevabi, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {

        Map<String, Object> hataCevabi = new HashMap<>();
        hataCevabi.put("zaman", LocalDateTime.now().toString());
        hataCevabi.put("durum", HttpStatus.BAD_REQUEST.value());
        hataCevabi.put("mesaj", ex.getMessage());

        return new ResponseEntity<>(hataCevabi, HttpStatus.BAD_REQUEST);
    }
}