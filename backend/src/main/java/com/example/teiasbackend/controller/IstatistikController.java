package com.example.teiasbackend.controller;

import com.example.teiasbackend.dto.IstatistikDto;
import com.example.teiasbackend.service.IstatistikService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.teiasbackend.entity.BasvuruDurumu;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "7. İstatistik & Sistem")
public class IstatistikController {

    private final IstatistikService istatistikService;

    @GetMapping("/istatistikler")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "[İK] Gösterge paneli istatistikleri")
    public IstatistikDto istatistikler() {
        return istatistikService.ozet();
    }

    @GetMapping("/sistem/bilgi")
    @Operation(summary = "Uygulama bilgisi ve başvuru durum etiketleri")
    public Map<String, Object> bilgi() {
        return Map.of(
                "uygulama", "TEİAŞ Lise Stajyer Başvuru ve Yönetim Portalı",
                "surum", "1.0.0",
                "durumlar", Arrays.stream(BasvuruDurumu.values())
                        .collect(Collectors.toMap(Enum::name, BasvuruDurumu::getEtiket))
        );
    }
}
