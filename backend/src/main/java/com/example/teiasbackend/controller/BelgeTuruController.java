package com.example.teiasbackend.controller;

import com.example.teiasbackend.dto.BelgeTuruRequestDto;
import com.example.teiasbackend.dto.BelgeTuruResponseDto;
import com.example.teiasbackend.dto.MesajDto;
import com.example.teiasbackend.service.BelgeTuruService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/belge-turleri")
@RequiredArgsConstructor
@Tag(name = "5. Belge Türleri", description = "Başvuruda istenen evrak tanımları")
public class BelgeTuruController {

    private final BelgeTuruService belgeTuruService;

    @GetMapping
    @Operation(summary = "Belge türlerini listele")
    public List<BelgeTuruResponseDto> listele(@RequestParam(defaultValue = "false") boolean tumu) {
        return belgeTuruService.listele(!tumu);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "[İK] Belge türü ekle")
    public BelgeTuruResponseDto olustur(@Valid @RequestBody BelgeTuruRequestDto dto) {
        return belgeTuruService.olustur(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "[İK] Belge türü güncelle")
    public BelgeTuruResponseDto guncelle(@PathVariable Long id, @Valid @RequestBody BelgeTuruRequestDto dto) {
        return belgeTuruService.guncelle(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "[İK] Belge türü sil")
    public MesajDto sil(@PathVariable Long id) {
        belgeTuruService.sil(id);
        return new MesajDto("Belge türü silindi.");
    }
}
