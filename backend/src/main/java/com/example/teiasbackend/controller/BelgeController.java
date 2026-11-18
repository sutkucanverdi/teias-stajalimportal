package com.example.teiasbackend.controller;

import com.example.teiasbackend.dto.BelgeDurumGuncelleRequestDto;
import com.example.teiasbackend.dto.BelgeResponseDto;
import com.example.teiasbackend.security.GirisYapanKullanici;
import com.example.teiasbackend.service.BelgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/belgeler")
@RequiredArgsConstructor
@Tag(name = "3. Belgeler", description = "Yüklenen belgeleri indirme ve İK değerlendirmesi")
public class BelgeController {

    private final BelgeService belgeService;

    @GetMapping("/{id}/indir")
    @Operation(summary = "Belgeyi indir / görüntüle")
    public ResponseEntity<Resource> indir(@AuthenticationPrincipal GirisYapanKullanici k, @PathVariable Long id,
                                          @RequestParam(defaultValue = "false") boolean ek) {
        BelgeService.IndirilecekBelge belge = belgeService.indir(k, id);
        ContentDisposition cd = (ek ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(belge.ad(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(belge.icerikTuru() != null ? MediaType.parseMediaType(belge.icerikTuru()) : MediaType.APPLICATION_OCTET_STREAM)
                .body(belge.kaynak());
    }

    @PutMapping("/{id}/durum")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "[İK] Belgeyi onayla / reddet", description = "Reddederken öğrenciye iletilecek not zorunludur.")
    public BelgeResponseDto durumGuncelle(@AuthenticationPrincipal GirisYapanKullanici k, @PathVariable Long id,
                                          @Valid @RequestBody BelgeDurumGuncelleRequestDto dto) {
        return belgeService.durumGuncelle(k, id, dto);
    }
}
