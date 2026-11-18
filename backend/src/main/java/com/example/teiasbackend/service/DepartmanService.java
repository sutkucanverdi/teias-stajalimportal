package com.example.teiasbackend.service;

import com.example.teiasbackend.dto.DepartmanRequestDto;
import com.example.teiasbackend.dto.DepartmanResponseDto;
import com.example.teiasbackend.entity.BasvuruDurumu;
import com.example.teiasbackend.entity.Departman;
import com.example.teiasbackend.exception.CakismaException;
import com.example.teiasbackend.exception.IsKuraliException;
import com.example.teiasbackend.exception.KayitBulunamadiException;
import com.example.teiasbackend.repository.BasvuruRepository;
import com.example.teiasbackend.repository.DepartmanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmanService {

    private final DepartmanRepository departmanRepository;
    private final BasvuruRepository basvuruRepository;

    @Transactional(readOnly = true)
    public List<DepartmanResponseDto> listele(boolean sadeceAktif) {
        Map<Long, Long> onaylananlar = basvuruRepository.departmanDoluluklari().stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> r[4] == null ? 0L : ((Number) r[4]).longValue()));
        List<Departman> liste = sadeceAktif ? departmanRepository.findByAktifTrueOrderByAdAsc() : departmanRepository.findAllByOrderByAdAsc();
        return liste.stream().map(d -> DepartmanResponseDto.from(d, onaylananlar.getOrDefault(d.getId(), 0L))).toList();
    }

    @Transactional(readOnly = true)
    public DepartmanResponseDto getir(Long id) {
        Departman d = bul(id);
        return DepartmanResponseDto.from(d, basvuruRepository.countByAtananDepartmanIdAndDurum(id, BasvuruDurumu.ONAYLANDI));
    }

    @Transactional
    public DepartmanResponseDto olustur(DepartmanRequestDto dto) {
        if (departmanRepository.existsByAdIgnoreCase(dto.ad().trim())) {
            throw new CakismaException("Bu isimde bir birim zaten mevcut.");
        }
        Departman d = new Departman();
        uygula(d, dto);
        return DepartmanResponseDto.from(departmanRepository.save(d), 0);
    }

    @Transactional
    public DepartmanResponseDto guncelle(Long id, DepartmanRequestDto dto) {
        Departman d = bul(id);
        if (departmanRepository.existsByAdIgnoreCaseAndIdNot(dto.ad().trim(), id)) {
            throw new CakismaException("Bu isimde bir birim zaten mevcut.");
        }
        long onaylanan = basvuruRepository.countByAtananDepartmanIdAndDurum(id, BasvuruDurumu.ONAYLANDI);
        if (dto.kontenjan() < onaylanan) {
            throw new IsKuraliException("Kontenjan, birime onaylanmış stajyer sayısının (" + onaylanan + ") altına düşürülemez.");
        }
        uygula(d, dto);
        return DepartmanResponseDto.from(d, onaylanan);
    }

    @Transactional
    public void sil(Long id) {
        Departman d = bul(id);
        if (basvuruRepository.existsByTercihDepartmanIdOrAtananDepartmanId(id, id)) {
            throw new IsKuraliException("Bu birime bağlı başvurular olduğu için silinemez. Bunun yerine birimi pasif hale getirebilirsiniz.");
        }
        departmanRepository.delete(d);
    }

    Departman bul(Long id) {
        return departmanRepository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Birim bulunamadı (id: " + id + ")."));
    }

    private static void uygula(Departman d, DepartmanRequestDto dto) {
        d.setAd(dto.ad().trim());
        d.setAciklama(dto.aciklama());
        d.setSehir(dto.sehir().trim());
        d.setKontenjan(dto.kontenjan());
        d.setAktif(dto.aktif() == null || dto.aktif());
    }
}
