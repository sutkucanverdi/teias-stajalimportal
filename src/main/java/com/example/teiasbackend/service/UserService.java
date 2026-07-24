package com.example.teiasbackend.service;

import com.example.teiasbackend.dto.StudentRegisterRequestDto;
import com.example.teiasbackend.entity.User;
import com.example.teiasbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String ogrenciKaydet(StudentRegisterRequestDto dto) {
        if (userRepository.findByTcKimlik(dto.getTcKimlik()).isPresent()) {
            throw new RuntimeException("Bu TC Kimlik numarası ile zaten kayıt olunmuş.");
        }

        User user = new User();
        user.setAd(dto.getAd());
        user.setSoyad(dto.getSoyad());
        user.setTcKimlik(dto.getTcKimlik());
        user.setKullaniciAdi(dto.getTcKimlik());
        user.setEmail(dto.getEmail());
        user.setTelefon(dto.getTelefon());
        user.setDogumTarihi(dto.getDogumTarihi());
        user.setSifre(passwordEncoder.encode(dto.getSifre()));
        user.setRol("ROLE_STUDENT");
        user.setAktifMi(false);

        String aktivasyonKodu = String.format("%06d", new Random().nextInt(999999));
        user.setAktivasyonKodu(aktivasyonKodu);

        userRepository.save(user);

        System.out.println("SİSTEM BİLDİRİMİ -> " + user.getEmail() + " için Aktivasyon Kodu: " + aktivasyonKodu);

        return "Kayıt başarılı. Sistemde oluşan aktivasyon kodunu kullanarak hesabınızı doğrulayınız.";
    }
}