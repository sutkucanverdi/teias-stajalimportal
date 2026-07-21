package com.example.teiasbackend.controller;

import com.example.teiasbackend.dto.BasvuruCreateRequestDto;
import com.example.teiasbackend.dto.BasvuruResponseDto;
import com.example.teiasbackend.service.BasvuruService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}