package com.example.teiasbackend.service;

import com.example.teiasbackend.dto.*;
import com.example.teiasbackend.entity.Rol;
import com.example.teiasbackend.entity.User;
import com.example.teiasbackend.exception.CakismaException;
import com.example.teiasbackend.exception.IsKuraliException;
import com.example.teiasbackend.repository.UserRepository;
import com.example.teiasbackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom RASTGELE = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.kayit.aktivasyon-gerekli:false}")
    private boolean aktivasyonGerekli;

    @Transactional
    public MesajDto ogrenciKaydet(StudentRegisterRequestDto dto) {
        String email = dto.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByTcKimlik(dto.tcKimlik()) || userRepository.existsByKullaniciAdi(dto.tcKimlik())) {
            throw new CakismaException("Bu TC Kimlik numarası ile sistemde zaten kayıtlı bir kullanıcı bulunmaktadır.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new CakismaException("Bu e-posta adresi ile sistemde zaten kayıtlı bir kullanıcı bulunmaktadır.");
        }

        User user = new User();
        user.setAd(dto.ad().trim());
        user.setSoyad(dto.soyad().trim());
        user.setTcKimlik(dto.tcKimlik());
        user.setKullaniciAdi(dto.tcKimlik());
        user.setEmail(email);
        user.setTelefon(telefonNormalize(dto.telefon()));
        user.setDogumTarihi(dto.dogumTarihi());
        user.setSifre(passwordEncoder.encode(dto.sifre()));
        user.setRol(Rol.ROLE_STUDENT);

        if (aktivasyonGerekli) {
            String kod = String.format("%06d", RASTGELE.nextInt(1_000_000));
            user.setAktifMi(false);
            user.setAktivasyonKodu(kod);
            userRepository.save(user);
            // E-posta altyapısı bağlanana kadar kod sunucu günlüğüne yazılır
            log.info("SİSTEM BİLDİRİMİ -> {} için aktivasyon kodu: {}", email, kod);
            return new MesajDto("Kayıt başarılı. E-posta adresinize gönderilen aktivasyon kodu ile hesabınızı doğrulayınız.");
        }

        user.setAktifMi(true);
        userRepository.save(user);
        return new MesajDto("Kayıt başarılı. TC Kimlik numaranız ve şifrenizle giriş yapabilirsiniz.");
    }

    @Transactional(readOnly = true)
    public LoginResponseDto girisYap(LoginRequestDto dto) {
        String giris = dto.kullaniciAdi().trim();
        Optional<User> bulunan = userRepository.findByKullaniciAdi(giris);
        if (bulunan.isEmpty() && giris.contains("@")) {
            bulunan = userRepository.findByEmailIgnoreCase(giris);
        }
        User user = bulunan
                .filter(u -> passwordEncoder.matches(dto.sifre(), u.getSifre()))
                .orElseThrow(() -> new BadCredentialsException("Geçersiz kullanıcı adı veya şifre!"));

        if (!user.isAktifMi()) {
            throw new DisabledException(user.getAktivasyonKodu() != null
                    ? "Hesabınız henüz aktive edilmemiş. Lütfen aktivasyon kodunuzu giriniz."
                    : "Hesabınız pasif durumdadır. Lütfen İnsan Kaynakları ile iletişime geçiniz.");
        }

        return new LoginResponseDto("Giriş başarılı! Hoş geldiniz, " + user.getAd(),
                jwtService.tokenUret(user), "Bearer", jwtService.getGecerlilikSaniye(), UserResponseDto.from(user));
    }

    @Transactional
    public MesajDto aktivasyon(AktivasyonRequestDto dto) {
        User user = userRepository.findByKullaniciAdi(dto.kullaniciAdi().trim())
                .orElseThrow(() -> new IsKuraliException("Aktivasyon kodu veya TC Kimlik No hatalı."));
        if (user.isAktifMi()) {
            return new MesajDto("Hesabınız zaten aktif.");
        }
        if (user.getAktivasyonKodu() == null || !user.getAktivasyonKodu().equals(dto.kod().trim())) {
            throw new IsKuraliException("Aktivasyon kodu veya TC Kimlik No hatalı.");
        }
        user.setAktifMi(true);
        user.setAktivasyonKodu(null);
        return new MesajDto("Hesabınız başarıyla aktive edildi. Giriş yapabilirsiniz.");
    }

    static String telefonNormalize(String telefon) {
        if (telefon == null) return null;
        String rakamlar = telefon.replaceAll("\\D", "");
        return rakamlar.length() == 10 ? "0" + rakamlar : rakamlar;
    }
}
