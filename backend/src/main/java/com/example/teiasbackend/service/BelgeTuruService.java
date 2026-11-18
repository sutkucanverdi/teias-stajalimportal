package com.example.teiasbackend.service;

import com.example.teiasbackend.dto.BelgeTuruRequestDto;
import com.example.teiasbackend.dto.BelgeTuruResponseDto;
import com.example.teiasbackend.entity.BelgeTuru;
import com.example.teiasbackend.exception.CakismaException;
import com.example.teiasbackend.exception.IsKuraliException;
import com.example.teiasbackend.exception.KayitBulunamadiException;
import com.example.teiasbackend.repository.BasvuruBelgesiRepository;
import com.example.teiasbackend.repository.BelgeTuruRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BelgeTuruService {

    private final BelgeTuruRepository belgeTuruRepository;
    private final BasvuruBelgesiRepository belgeRepository;

    @Transactional(readOnly = true)
    public List<BelgeTuruResponseDto> listele(boolean sadeceAktif) {
        return (sadeceAktif ? belgeTuruRepository.findByAktifTrueOrderByIdAsc() : belgeTuruRepository.findAllByOrderByIdAsc())
                .stream().map(BelgeTuruResponseDto::from).toList();
    }

    @Transactional
    public BelgeTuruResponseDto olustur(BelgeTuruRequestDto dto) {
        if (belgeTuruRepository.existsByKodIgnoreCase(dto.kod())) {
            throw new CakismaException("Bu kod ile bir belge türü zaten mevcut.");
        }
        BelgeTuru b = new BelgeTuru();
        b.setKod(dto.kod());
        uygula(b, dto);
        return BelgeTuruResponseDto.from(belgeTuruRepository.save(b));
    }

    @Transactional
    public BelgeTuruResponseDto guncelle(Long id, BelgeTuruRequestDto dto) {
        BelgeTuru b = bul(id);
        if (!b.getKod().equalsIgnoreCase(dto.kod())) {
            throw new IsKuraliException("Belge türü kodu değiştirilemez.");
        }
        uygula(b, dto);
        return BelgeTuruResponseDto.from(b);
    }

    @Transactional
    public void sil(Long id) {
        BelgeTuru b = bul(id);
        if (belgeRepository.existsByBelgeTuruId(id)) {
            throw new IsKuraliException("Bu türde yüklenmiş belgeler olduğu için silinemez. Bunun yerine pasif hale getirebilirsiniz.");
        }
        belgeTuruRepository.delete(b);
    }

    BelgeTuru bul(Long id) {
        return belgeTuruRepository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Belge türü bulunamadı (id: " + id + ")."));
    }

    private static void uygula(BelgeTuru b, BelgeTuruRequestDto dto) {
        b.setAd(dto.ad().trim());
        b.setAciklama(dto.aciklama());
        b.setZorunlu(dto.zorunlu());
        b.setMaxBoyutMb(dto.maxBoyutMb());
        b.setAktif(dto.aktif() == null || dto.aktif());
    }
}
