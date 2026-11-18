package com.example.teiasbackend.service;

import com.example.teiasbackend.dto.*;
import com.example.teiasbackend.entity.Departman;
import com.example.teiasbackend.entity.Rol;
import com.example.teiasbackend.entity.User;
import com.example.teiasbackend.exception.CakismaException;
import com.example.teiasbackend.exception.IsKuraliException;
import com.example.teiasbackend.exception.KayitBulunamadiException;
import com.example.teiasbackend.repository.DepartmanRepository;
import com.example.teiasbackend.repository.UserRepository;
import com.example.teiasbackend.security.GirisYapanKullanici;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DepartmanRepository departmanRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponseDto benimBilgilerim(GirisYapanKullanici k) {
        return UserResponseDto.from(bul(k.id()));
    }

    @Transactional
    public UserResponseDto profilGuncelle(GirisYapanKullanici k, ProfilGuncelleRequestDto dto) {
        User user = bul(k.id());
        String email = dto.email().trim().toLowerCase(Locale.ROOT);
        if (!email.equalsIgnoreCase(Objects.toString(user.getEmail(), "")) && userRepository.existsByEmailIgnoreCase(email)) {
            throw new CakismaException("Bu e-posta adresi başka bir kullanıcı tarafından kullanılıyor.");
        }
        user.setEmail(email);
        user.setTelefon(AuthService.telefonNormalize(dto.telefon()));
        return UserResponseDto.from(user);
    }

    @Transactional
    public MesajDto sifreDegistir(GirisYapanKullanici k, SifreDegistirRequestDto dto) {
        User user = bul(k.id());
        if (!passwordEncoder.matches(dto.mevcutSifre(), user.getSifre())) {
            throw new IsKuraliException("Mevcut şifreniz hatalı.");
        }
        if (passwordEncoder.matches(dto.yeniSifre(), user.getSifre())) {
            throw new IsKuraliException("Yeni şifre mevcut şifre ile aynı olamaz.");
        }
        user.setSifre(passwordEncoder.encode(dto.yeniSifre()));
        return new MesajDto("Şifreniz başarıyla değiştirildi.");
    }

    // ---------------- İK: kullanıcı yönetimi ----------------

    @Transactional(readOnly = true)
    public List<UserResponseDto> listele(Rol rol) {
        List<User> liste = rol == null ? userRepository.findAllByOrderByOlusturmaTarihiDesc() : userRepository.findByRolOrderByAdAsc(rol);
        return liste.stream().map(UserResponseDto::from).toList();
    }

    @Transactional
    public UserResponseDto personelOlustur(PersonelCreateRequestDto dto) {
        if (dto.rol() == Rol.ROLE_STUDENT) {
            throw new IsKuraliException("Öğrenci hesapları yalnızca kayıt ekranından oluşturulabilir.");
        }
        if (userRepository.existsByKullaniciAdi(dto.kullaniciAdi())) {
            throw new CakismaException("Bu kullanıcı adı zaten kullanılıyor.");
        }
        String email = dto.email() == null || dto.email().isBlank() ? null : dto.email().trim().toLowerCase(Locale.ROOT);
        if (email != null && userRepository.existsByEmailIgnoreCase(email)) {
            throw new CakismaException("Bu e-posta adresi zaten kullanılıyor.");
        }
        Departman departman = null;
        if (dto.rol() == Rol.ROLE_BIRIM) {
            if (dto.departmanId() == null) {
                throw new IsKuraliException("Birim sorumlusu için birim seçilmesi zorunludur.");
            }
            departman = departmanRepository.findById(dto.departmanId())
                    .orElseThrow(() -> new KayitBulunamadiException("Birim bulunamadı."));
        }

        User user = new User();
        user.setAd(dto.ad().trim());
        user.setSoyad(dto.soyad().trim());
        user.setKullaniciAdi(dto.kullaniciAdi());
        user.setEmail(email);
        user.setTelefon(dto.telefon());
        user.setSifre(passwordEncoder.encode(dto.sifre()));
        user.setRol(dto.rol());
        user.setDepartman(departman);
        user.setAktifMi(true);
        return UserResponseDto.from(userRepository.save(user));
    }

    @Transactional
    public UserResponseDto aktiflikDegistir(GirisYapanKullanici islemYapan, Long id, boolean aktif) {
        if (islemYapan.id().equals(id) && !aktif) {
            throw new IsKuraliException("Kendi hesabınızı pasif hale getiremezsiniz.");
        }
        User user = bul(id);
        user.setAktifMi(aktif);
        if (aktif) {
            user.setAktivasyonKodu(null);
        }
        return UserResponseDto.from(user);
    }

    private User bul(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Kullanıcı bulunamadı."));
    }
}
