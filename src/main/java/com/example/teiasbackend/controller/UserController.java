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
    @GetMapping
    public List<UserResponseDto> tumKullanicilariGetir() {
        return userService.tumKullanicilariGetir();
    }
    @PostMapping
    public UserResponseDto yeniKullaniciEkle(@RequestBody UserCreateRequestDto requestDto) {
        return userService.kullaniciKaydet(requestDto);
    }
    @GetMapping("/test-ekle")
    public String testKullanicisiEkle() {
        try {
            UserCreateRequestDto dto = new UserCreateRequestDto();
            dto.setAdSoyad("semih utku");
            dto.setTcKimlik("10894256230");
            dto.setSifre("GizliSifre123!");

            userService.kullaniciKaydet(dto);
            return "BAŞARILI! DTO üzerinden kullanıcı oluşturuldu ve şifresi gizlenerek veritabanına işlendi! 🎉";
        } catch (Exception e) {
            return "ALGORİTMA ENGELLEDİ! 🛑 " + e.getMessage();
        }
    }
}