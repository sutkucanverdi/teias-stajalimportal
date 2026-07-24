package com.example.teiasbackend.config;

import com.example.teiasbackend.entity.User;
import com.example.teiasbackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByKullaniciAdi("ik_admin").isEmpty()) {

                User ikYonetici = new User();
                ikYonetici.setAdSoyad("TEİAŞ İK Yöneticisi");
                ikYonetici.setKullaniciAdi("ik_admin");
                ikYonetici.setSifre("teias2026");
                ikYonetici.setRol("ROLE_HR");
                ikYonetici.setTcKimlik("10000000078");
                userRepository.save(ikYonetici);
                System.out.println("✅ SİSTEM BİLDİRİMİ: Varsayılan İK Yönetici Hesabı başarıyla oluşturuldu!");
                System.out.println("Kullanıcı Adı: ik_admin | Şifre: teias2026");
            }
        };
    }
}