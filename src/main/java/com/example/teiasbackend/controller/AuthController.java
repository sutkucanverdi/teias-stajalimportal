package com.example.teiasbackend.controller;

import com.example.teiasbackend.entity.User;
import com.example.teiasbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UserRepository userRepository;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginIstegi) {
        String kullaniciAdi = loginIstegi.get("kullaniciAdi");
        String sifre = loginIstegi.get("sifre");

        Optional<User> userOptional = userRepository.findByKullaniciAdi(kullaniciAdi);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getSifre().equals(sifre)) {
                return ResponseEntity.ok(Map.of(
                        "mesaj", "Giriş Başarılı! Hoş geldin, " + user.getAdSoyad(),
                        "rol", user.getRol(),
                        "kullaniciAdi", user.getKullaniciAdi()
                ));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("hata", "Geçersiz kullanıcı adı veya şifre!"));
    }
}