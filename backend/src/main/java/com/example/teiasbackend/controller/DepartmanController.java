package com.example.teiasbackend.controller;

import com.example.teiasbackend.dto.DepartmanRequestDto;
import com.example.teiasbackend.dto.DepartmanResponseDto;
import com.example.teiasbackend.dto.MesajDto;
import com.example.teiasbackend.service.DepartmanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departmanlar")
@RequiredArgsConstructor
@Tag(name = "4. Birimler", description = "Staj yapılabilecek TEİAŞ birimleri ve kontenjanları")
public class DepartmanController {

    private final DepartmanService departmanService;

    @GetMapping
    @Operation(summary = "Birimleri listele", description = "Varsayılan olarak yalnızca aktif birimler döner. Tümü için tumu=true (İK).")
    public List<DepartmanResponseDto> listele(@RequestParam(defaultValue = "false") boolean tumu) {
        return departmanService.listele(!tumu);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Birim detayı")
    public DepartmanResponseDto getir(@PathVariable Long id) {
        return departmanService.getir(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "[İK] Birim ekle")
    public DepartmanResponseDto olustur(@Valid @RequestBody DepartmanRequestDto dto) {
        return departmanService.olustur(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "[İK] Birim güncelle")
    public DepartmanResponseDto guncelle(@PathVariable Long id, @Valid @RequestBody DepartmanRequestDto dto) {
        return departmanService.guncelle(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "[İK] Birim sil", description = "Bağlı başvurusu olan birim silinemez; pasif yapılabilir.")
    public MesajDto sil(@PathVariable Long id) {
        departmanService.sil(id);
        return new MesajDto("Birim silindi.");
    }
}
