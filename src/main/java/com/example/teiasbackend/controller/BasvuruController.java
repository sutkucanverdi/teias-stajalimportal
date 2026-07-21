package com.example.teiasbackend.controller;

import com.example.teiasbackend.dto.BasvuruCreateRequestDto;
import com.example.teiasbackend.dto.BasvuruResponseDto;
import com.example.teiasbackend.service.BasvuruService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.teiasbackend.dto.BasvuruDurumGuncelleRequestDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
@RestController
@RequestMapping("/api/basvurular")
@RequiredArgsConstructor
public class BasvuruController {

    private final BasvuruService basvuruService;

    // 🌟 Dış dünyadan (Swagger veya React) HTTP POST ile gelen başvuru paketini karşılar:
    @PostMapping
    public ResponseEntity<BasvuruResponseDto> yeniBasvuruEkle(@RequestBody BasvuruCreateRequestDto requestDto) {
        BasvuruResponseDto response = basvuruService.basvuruOlustur(requestDto);
        // Yeni bir kaynak oluşturulduğu için 201 Created (Kayıt Başarıyla Oluşturuldu) koduyla dönüyoruz!
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    // 🌟 İK Yöneticisinin (veya Swagger'ın) HTTP GET ile tüm listeyi çekmesini sağlar:
    @GetMapping
    public ResponseEntity<List<BasvuruResponseDto>> tumBasvurulariListele() {
        List<BasvuruResponseDto> basvuruListesi = basvuruService.tumBasvurulariGetir();
        // Liste başarılı şekilde çekildiği için 200 OK koduyla birlikte listeyi dönüyoruz!
        return ResponseEntity.ok(basvuruListesi);
    }
    // 🌟 İK Yöneticisinin URL üzerinden ID belirterek başvuru durumunu değiştirmesini sağlar:
    // Örn: PUT http://localhost:8080/api/basvurular/1/durum
    @PutMapping("/{id}/durum")
    public ResponseEntity<BasvuruResponseDto> basvuruDurumunuDegistir(
            @PathVariable Long id,
            @RequestBody BasvuruDurumGuncelleRequestDto requestDto) {

        BasvuruResponseDto guncelBasvuru = basvuruService.basvuruDurumGuncelle(id, requestDto);
        return ResponseEntity.ok(guncelBasvuru);
    }
}