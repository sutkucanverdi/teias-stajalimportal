package com.example.teiasbackend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/** Tüm hata cevaplarının ortak gövdesi */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiHata(
        LocalDateTime zaman,
        int durum,
        String hata,
        String mesaj,
        String yol,
        Map<String, String> alanHatalari
) {
    public static ApiHata of(int durum, String hata, String mesaj, String yol) {
        return new ApiHata(LocalDateTime.now(), durum, hata, mesaj, yol, null);
    }
}
