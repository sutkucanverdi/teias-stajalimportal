package com.example.teiasbackend.controller;

import com.example.teiasbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Eski test metotları tamamen temizlendi.
    // İleride "Kullanıcı Profilini Görüntüle", "Şifre Değiştir" gibi
    // giriş yapmış (Token'ı olan) kişilerin yapacağı işlemleri buraya inşa edeceğiz.
}