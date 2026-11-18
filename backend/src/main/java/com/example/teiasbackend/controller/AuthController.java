package com.example.teiasbackend.controller;

import com.example.teiasbackend.dto.*;
import com.example.teiasbackend.security.GirisYapanKullanici;
import com.example.teiasbackend.service.AuthService;
import com.example.teiasbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "1. Kimlik Doğrulama", description = "Kayıt, giriş ve hesap işlemleri")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "Giriş yap", description = "Öğrenciler TC Kimlik No (veya e-posta), personel kurum kullanıcı adı ile giriş yapar. JWT döner.")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto dto) {
        return authService.girisYap(dto);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Öğrenci kaydı", description = "Lise öğrencisi için hesap oluşturur. TC Kimlik No resmi algoritma ile doğrulanır.")
    public MesajDto register(@Valid @RequestBody StudentRegisterRequestDto dto) {
        return authService.ogrenciKaydet(dto);
    }

    @PostMapping("/aktivasyon")
    @Operation(summary = "Hesap aktivasyonu", description = "Aktivasyon zorunluysa kayıt sonrası üretilen kod ile hesabı aktifleştirir.")
    public MesajDto aktivasyon(@Valid @RequestBody AktivasyonRequestDto dto) {
        return authService.aktivasyon(dto);
    }

    @GetMapping("/me")
    @Operation(summary = "Oturumdaki kullanıcı bilgisi")
    public UserResponseDto me(@AuthenticationPrincipal GirisYapanKullanici k) {
        return userService.benimBilgilerim(k);
    }

    @PutMapping("/me")
    @Operation(summary = "İletişim bilgilerini güncelle")
    public UserResponseDto profilGuncelle(@AuthenticationPrincipal GirisYapanKullanici k, @Valid @RequestBody ProfilGuncelleRequestDto dto) {
        return userService.profilGuncelle(k, dto);
    }

    @PutMapping("/sifre")
    @Operation(summary = "Şifre değiştir")
    public MesajDto sifreDegistir(@AuthenticationPrincipal GirisYapanKullanici k, @Valid @RequestBody SifreDegistirRequestDto dto) {
        return userService.sifreDegistir(k, dto);
    }
}
