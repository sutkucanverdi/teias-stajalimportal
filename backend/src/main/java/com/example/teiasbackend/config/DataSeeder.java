package com.example.teiasbackend.config;

import com.example.teiasbackend.entity.Rol;
import com.example.teiasbackend.entity.User;
import com.example.teiasbackend.repository.DepartmanRepository;
import com.example.teiasbackend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Uygulama ilk açıldığında varsayılan İK yöneticisini (ve istenirse demo hesapları) oluşturur. */
@Slf4j
@Configuration
public class DataSeeder {

    @Value("${app.seed.ik-kullanici-adi}")
    private String ikKullaniciAdi;

    @Value("${app.seed.ik-sifre}")
    private String ikSifre;

    @Value("${app.seed.demo-veri}")
    private boolean demoVeri;

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, DepartmanRepository departmanRepository, PasswordEncoder encoder) {
        return new CommandLineRunner() {
            @Override
            @Transactional
            public void run(String... args) {
                if (userRepository.findByKullaniciAdi(ikKullaniciAdi).isEmpty()) {
                    User ik = new User();
                    ik.setAd("TEİAŞ İK");
                    ik.setSoyad("Yöneticisi");
                    ik.setKullaniciAdi(ikKullaniciAdi);
                    ik.setEmail("ik@teias.local");
                    ik.setSifre(encoder.encode(ikSifre));
                    ik.setRol(Rol.ROLE_HR);
                    ik.setAktifMi(true);
                    userRepository.save(ik);
                    log.info("✅ SİSTEM BİLDİRİMİ: Varsayılan İK yönetici hesabı oluşturuldu -> kullanıcı adı: {}", ikKullaniciAdi);
                }

                if (!demoVeri) {
                    return;
                }

                if (userRepository.findByKullaniciAdi("bt_birim").isEmpty()) {
                    departmanRepository.findAllByOrderByAdAsc().stream()
                            .filter(d -> d.getAd().startsWith("Bilgi Teknolojileri"))
                            .findFirst()
                            .ifPresent(bt -> {
                                User birim = new User();
                                birim.setAd("Bilgi Teknolojileri");
                                birim.setSoyad("Birim Sorumlusu");
                                birim.setKullaniciAdi("bt_birim");
                                birim.setEmail("bt.birim@teias.local");
                                birim.setSifre(encoder.encode("teias2026"));
                                birim.setRol(Rol.ROLE_BIRIM);
                                birim.setDepartman(bt);
                                birim.setAktifMi(true);
                                userRepository.save(birim);
                                log.info("✅ Demo birim sorumlusu oluşturuldu -> bt_birim / teias2026");
                            });
                }

                if (userRepository.findByKullaniciAdi("10000001440").isEmpty()) {
                    User ogrenci = new User();
                    ogrenci.setAd("Ahmet");
                    ogrenci.setSoyad("Yılmaz");
                    ogrenci.setTcKimlik("10000001440");
                    ogrenci.setKullaniciAdi("10000001440");
                    ogrenci.setEmail("ahmet.yilmaz@ornek.com");
                    ogrenci.setTelefon("05551234567");
                    ogrenci.setDogumTarihi(LocalDate.of(2009, 3, 15));
                    ogrenci.setSifre(encoder.encode("Ogrenci2026"));
                    ogrenci.setRol(Rol.ROLE_STUDENT);
                    ogrenci.setAktifMi(true);
                    userRepository.save(ogrenci);
                    log.info("✅ Demo öğrenci oluşturuldu -> 10000001440 / Ogrenci2026");
                }
            }
        };
    }
}
