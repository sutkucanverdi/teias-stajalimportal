package com.example.teiasbackend.controller;

import com.example.teiasbackend.dto.BasvuruCreateRequestDto;
import com.example.teiasbackend.dto.BasvuruResponseDto;
import com.example.teiasbackend.dto.BasvuruDurumGuncelleRequestDto;
import com.example.teiasbackend.dto.BasvuruGuncelleRequestDto;
import com.example.teiasbackend.service.BasvuruService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/basvurular")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class BasvuruController {

    private final BasvuruService basvuruService;

    // 🚀 Lise Staj Başvurusunu ve PDF Belgelerini Karşılayan Uç Nokta
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BasvuruResponseDto> yeniBasvuruEkle(
            @ModelAttribute BasvuruCreateRequestDto requestDto,
            @RequestParam(value = "isg", required = false) MultipartFile isg,
            @RequestParam(value = "okul", required = false) MultipartFile okul,
            @RequestParam(value = "veli", required = false) MultipartFile veli) {

        BasvuruResponseDto response = basvuruService.basvuruOlustur(requestDto, isg, okul, veli);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BasvuruResponseDto>> tumBasvurulariListele() {
        List<BasvuruResponseDto> basvuruListesi = basvuruService.tumBasvurulariGetir();
        return ResponseEntity.ok(basvuruListesi);
    }

    @PutMapping("/{id}/durum")
    public ResponseEntity<BasvuruResponseDto> basvuruDurumunuDegistir(
            @PathVariable Long id,
            @RequestBody BasvuruDurumGuncelleRequestDto requestDto) {
        BasvuruResponseDto guncelBasvuru = basvuruService.basvuruDurumGuncelle(id, requestDto);
        return ResponseEntity.ok(guncelBasvuru);
    }

    @PutMapping(value = "/{id}/duzenle", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BasvuruResponseDto> basvuruDuzenle(
            @PathVariable Long id,
            @RequestParam(value = "departman", required = false) String departman,
            @RequestParam(value = "dosya", required = false) MultipartFile dosya) {

        BasvuruResponseDto response = basvuruService.basvuruGuncelleVeDosyaYukle(id, departman, dosya);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filtre/durum")
    public ResponseEntity<List<BasvuruResponseDto>> durumaGoreFiltrele(@RequestParam String durum) {
        return ResponseEntity.ok(basvuruService.durumaGoreBasvurulariGetir(durum));
    }

    @GetMapping("/filtre/departman")
    public ResponseEntity<List<BasvuruResponseDto>> departmanaGoreFiltrele(@RequestParam String departman) {
        return ResponseEntity.ok(basvuruService.departmanaGoreBasvurulariGetir(departman));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> basvuruSil(@PathVariable Long id) {
        String mesaj = basvuruService.basvuruSil(id);
        return ResponseEntity.ok(mesaj);
    }
    @GetMapping("/akilli-filtre")
    public ResponseEntity<List<BasvuruResponseDto>> akilliFiltrele(
            @RequestParam(required = false) String durum,
            @RequestParam(required = false) String departman,
            @RequestParam(required = false) String sehir) {

        List<BasvuruResponseDto> sonuclar = basvuruService.dinamikBasvuruFiltrele(durum, departman, sehir);
        return ResponseEntity.ok(sonuclar);
    }
}