package com.example.teiasbackend.service;

import com.example.teiasbackend.dto.IstatistikDto;
import com.example.teiasbackend.entity.BasvuruDurumu;
import com.example.teiasbackend.entity.Rol;
import com.example.teiasbackend.repository.BasvuruRepository;
import com.example.teiasbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IstatistikService {

    private final BasvuruRepository basvuruRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public IstatistikDto ozet() {
        Map<BasvuruDurumu, Long> sayilar = new EnumMap<>(BasvuruDurumu.class);
        for (BasvuruDurumu d : BasvuruDurumu.values()) {
            sayilar.put(d, 0L);
        }
        for (Object[] satir : basvuruRepository.durumaGoreSayilar()) {
            sayilar.put((BasvuruDurumu) satir[0], ((Number) satir[1]).longValue());
        }
        Map<String, Long> durumaGore = new LinkedHashMap<>();
        sayilar.forEach((d, s) -> {
            if (d != BasvuruDurumu.TASLAK) durumaGore.put(d.name(), s);
        });

        List<IstatistikDto.DepartmanDoluluk> departmanlar = basvuruRepository.departmanDoluluklari().stream()
                .map(r -> {
                    int kontenjan = ((Number) r[3]).intValue();
                    long onaylanan = r[4] == null ? 0 : ((Number) r[4]).longValue();
                    long degerlendirmede = r[5] == null ? 0 : ((Number) r[5]).longValue();
                    int yuzde = kontenjan == 0 ? 0 : (int) Math.min(100, Math.round(onaylanan * 100.0 / kontenjan));
                    return new IstatistikDto.DepartmanDoluluk((Long) r[0], (String) r[1], (String) r[2], kontenjan, onaylanan, degerlendirmede, yuzde);
                }).toList();

        long toplam = durumaGore.values().stream().mapToLong(Long::longValue).sum();
        return new IstatistikDto(
                toplam,
                userRepository.countByRol(Rol.ROLE_STUDENT),
                sayilar.get(BasvuruDurumu.GONDERILDI) + sayilar.get(BasvuruDurumu.IK_INCELEMESINDE),
                sayilar.get(BasvuruDurumu.BIRIM_DEGERLENDIRMESINDE),
                sayilar.get(BasvuruDurumu.ONAYLANDI),
                sayilar.get(BasvuruDurumu.REDDEDILDI),
                durumaGore,
                departmanlar
        );
    }
}
