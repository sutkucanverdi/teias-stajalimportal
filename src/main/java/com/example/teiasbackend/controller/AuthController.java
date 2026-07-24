package com.example.teiasbackend.controller;

import com.example.teiasbackend.dto.StudentRegisterRequestDto;
import com.example.teiasbackend.entity.User;
import com.example.teiasbackend.repository.UserRepository;
import com.example.teiasbackend.security.JwtService;
import com.example.teiasbackend.service.UserService;
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
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginIstegi) {
        String kullaniciAdi = loginIstegi.get("kullaniciAdi");
        String sifre = loginIstegi.get("sifre");

        Optional<User> userOptional = userRepository.findByKullaniciAdi(kullaniciAdi);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user.getSifre().equals(sifre)) {
                String uretilenToken = jwtService.tokenUret(user.getKullaniciAdi(), user.getRol());

                return ResponseEntity.ok(Map.of(
                        "mesaj", "Giriş Başarılı! Hoş geldin, " + user.getAd(),
                        "token", uretilenToken,
                        "rol", user.getRol(),
                        "kullaniciAdi", user.getKullaniciAdi()
                ));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("hata", "Geçersiz kullanıcı adı veya şifre!"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody StudentRegisterRequestDto requestDto) {
        try {
            String mesaj = userService.ogrenciKaydet(requestDto);
            return ResponseEntity.ok(Map.of("mesaj", mesaj));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("hata", e.getMessage()));
        }
    }
}