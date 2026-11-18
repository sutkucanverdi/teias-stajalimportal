package com.example.teiasbackend.controller;

import com.example.teiasbackend.dto.AktiflikRequestDto;
import com.example.teiasbackend.dto.PersonelCreateRequestDto;
import com.example.teiasbackend.dto.UserResponseDto;
import com.example.teiasbackend.entity.Rol;
import com.example.teiasbackend.security.GirisYapanKullanici;
import com.example.teiasbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('HR')")
@Tag(name = "6. Kullanıcı Yönetimi", description = "[İK] Personel hesapları ve kullanıcı aktiflik yönetimi")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "[İK] Kullanıcıları listele", description = "rol parametresi ile filtrelenebilir (ROLE_STUDENT, ROLE_HR, ROLE_BIRIM).")
    public List<UserResponseDto> listele(@RequestParam(required = false) Rol rol) {
        return userService.listele(rol);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "[İK] Personel hesabı oluştur (İK veya Birim Sorumlusu)")
    public UserResponseDto personelOlustur(@Valid @RequestBody PersonelCreateRequestDto dto) {
        return userService.personelOlustur(dto);
    }

    @PutMapping("/{id}/aktiflik")
    @Operation(summary = "[İK] Kullanıcıyı aktif / pasif yap")
    public UserResponseDto aktiflik(@AuthenticationPrincipal GirisYapanKullanici k, @PathVariable Long id,
                                    @Valid @RequestBody AktiflikRequestDto dto) {
        return userService.aktiflikDegistir(k, id, dto.aktif());
    }
}
