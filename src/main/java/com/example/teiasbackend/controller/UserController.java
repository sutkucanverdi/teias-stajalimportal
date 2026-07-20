package com.example.teiasbackend.controller;

import com.example.teiasbackend.dto.UserCreateRequestDto;
import com.example.teiasbackend.dto.UserResponseDto;
import com.example.teiasbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. SİPARİŞ: Tüm kullanıcıları ŞİFRELER GİZLİ şekilde listele
    @GetMapping
    public List<UserResponseDto> tumKullanicilariGetir() {
        return userService.tumKullanicilariGetir();
    }

    // 2. SİPARİŞ: Dış dünyadan (Postman / React vb.) gelen gerçek kayıt isteği
    @PostMapping
    public UserResponseDto yeniKullaniciEkle(@RequestBody UserCreateRequestDto requestDto) {
        return userService.kullaniciKaydet(requestDto);
    }

    // 🌟 TARAYICI TEST MODU: DTO üzerinden sahte bir kayıt gönderelim
    @GetMapping("/test-ekle")
    public String testKullanicisiEkle() {
        try {
            UserCreateRequestDto dto = new UserCreateRequestDto();
            dto.setAdSoyad("semih utku");
            dto.setTcKimlik("10894256230"); // Kurallara uygun gerçek algoritma formatı
            dto.setSifre("GizliSifre123!");

            userService.kullaniciKaydet(dto);
            return "BAŞARILI! DTO üzerinden kullanıcı oluşturuldu ve şifresi gizlenerek veritabanına işlendi! 🎉";
        } catch (Exception e) {
            return "ALGORİTMA ENGELLEDİ! 🛑 " + e.getMessage();
        }
    }
}