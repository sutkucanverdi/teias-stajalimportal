package com.example.teiasbackend.service;

import com.example.teiasbackend.dto.UserCreateRequestDto;
import com.example.teiasbackend.dto.UserResponseDto;
import com.example.teiasbackend.entity.User;
import com.example.teiasbackend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponseDto kullaniciKaydet(UserCreateRequestDto requestDto) {
        if (!tcKimlikDogrula(requestDto.getTcKimlik())) {
            throw new RuntimeException("HATA: Geçersiz TC Kimlik Numarası! Lütfen kurallara uygun bir TC girin.");
        }

        User yeniUser = new User();
        yeniUser.setTcKimlik(requestDto.getTcKimlik());
        yeniUser.setAdSoyad(requestDto.getAdSoyad());

        String kriptoluSifre = passwordEncoder.encode(requestDto.getSifre());
        yeniUser.setSifre(kriptoluSifre);

        yeniUser.setRol("STUDENT");

        User kaydedilenUser = userRepository.save(yeniUser);

        return entityToResponseDto(kaydedilenUser);
    }

    public List<UserResponseDto> tumKullanicilariGetir() {
        List<User> userListesi = userRepository.findAll();
        List<UserResponseDto> responseListesi = new ArrayList<>();

        for (User user : userListesi) {
            responseListesi.add(entityToResponseDto(user));
        }
        return responseListesi;
    }

    private UserResponseDto entityToResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setTcKimlik(user.getTcKimlik());
        dto.setAdSoyad(user.getAdSoyad());
        dto.setRol(user.getRol());
        return dto;
    }

    public boolean tcKimlikDogrula(String tc) {
        if (tc == null || tc.length() != 11) return false;
        if (!tc.matches("^[1-9][0-9]{10}$")) return false;

        int[] digits = new int[11];
        for (int i = 0; i < 11; i++) digits[i] = Character.getNumericValue(tc.charAt(i));

        int teklerToplami = digits[0] + digits[2] + digits[4] + digits[6] + digits[8];
        int ciftlerToplami = digits[1] + digits[3] + digits[5] + digits[7];

        int hane10 = ((teklerToplami * 7) - ciftlerToplami) % 10;
        if (hane10 < 0) hane10 += 10;
        if (hane10 != digits[9]) return false;

        int ilk10Toplam = 0;
        for (int i = 0; i < 10; i++) ilk10Toplam += digits[i];

        return (ilk10Toplam % 10) == digits[10];
    }
}