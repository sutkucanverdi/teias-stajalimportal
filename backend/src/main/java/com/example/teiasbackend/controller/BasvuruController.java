package com.example.teiasbackend.controller;

import com.example.teiasbackend.dto.*;
import com.example.teiasbackend.entity.BasvuruDurumu;
import com.example.teiasbackend.entity.StajTuru;
import com.example.teiasbackend.security.GirisYapanKullanici;
import com.example.teiasbackend.service.BasvuruService;
import com.example.teiasbackend.service.BelgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/basvurular")
@RequiredArgsConstructor
@Tag(name = "2. Başvurular", description = "Staj başvurusu oluşturma, belge yükleme ve değerlendirme iş akışı")
public class BasvuruController {

    private final BasvuruService basvuruService;
    private final BelgeService belgeService;

    // ------------------------- Öğrenci -------------------------

    @GetMapping("/benim")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "[Öğrenci] Başvurularım")
    public List<BasvuruResponseDto> benimBasvurularim(@AuthenticationPrincipal GirisYapanKullanici k) {
        return basvuruService.benimBasvurularim(k);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "[Öğrenci] Yeni başvuru (taslak) oluştur")
    public BasvuruResponseDto olustur(@AuthenticationPrincipal GirisYapanKullanici k, @Valid @RequestBody BasvuruCreateRequestDto dto) {
        return basvuruService.olustur(k, dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "[Öğrenci] Taslak / eksik belge durumundaki başvuruyu güncelle")
    public BasvuruResponseDto guncelle(@AuthenticationPrincipal GirisYapanKullanici k, @PathVariable Long id,
                                       @Valid @RequestBody BasvuruCreateRequestDto dto) {
        return basvuruService.guncelle(k, id, dto);
    }

    @PostMapping("/{id}/gonder")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "[Öğrenci] Başvuruyu İK'ya gönder", description = "Tüm zorunlu belgeler yüklenmiş olmalıdır.")
    public BasvuruResponseDto gonder(@AuthenticationPrincipal GirisYapanKullanici k, @PathVariable Long id) {
        return basvuruService.gonder(k, id);
    }

    @PostMapping(value = "/{id}/belgeler", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "[Öğrenci] Belge yükle (PDF/JPG/PNG)", description = "Aynı türde onaylanmamış belge varsa yenisiyle değiştirilir.")
    public BelgeResponseDto belgeYukle(@AuthenticationPrincipal GirisYapanKullanici k, @PathVariable Long id,
                                       @RequestParam Long belgeTuruId, @RequestParam("dosya") MultipartFile dosya) {
        return belgeService.yukle(k, id, belgeTuruId, dosya);
    }

    @DeleteMapping("/{id}/belgeler/{belgeId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "[Öğrenci] Yüklenen belgeyi sil")
    public MesajDto belgeSil(@AuthenticationPrincipal GirisYapanKullanici k, @PathVariable Long id, @PathVariable Long belgeId) {
        return belgeService.sil(k, id, belgeId);
    }

    // ------------------------- Ortak -------------------------

    @GetMapping("/{id}")
    @Operation(summary = "Başvuru detayı", description = "Öğrenci kendi başvurusunu, birim kendisine atananları, İK tümünü görür.")
    public BasvuruResponseDto getir(@AuthenticationPrincipal GirisYapanKullanici k, @PathVariable Long id) {
        return basvuruService.getir(k, id);
    }

    @GetMapping("/{id}/gecmis")
    @Operation(summary = "Başvuru durum geçmişi (zaman çizelgesi)")
    public List<GecmisResponseDto> gecmis(@AuthenticationPrincipal GirisYapanKullanici k, @PathVariable Long id) {
        return basvuruService.gecmis(k, id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','HR')")
    @Operation(summary = "Başvuruyu sil", description = "Öğrenci yalnızca taslağını, İK her başvuruyu silebilir.")
    public MesajDto sil(@AuthenticationPrincipal GirisYapanKullanici k, @PathVariable Long id) {
        return basvuruService.sil(k, id);
    }

    // ------------------------- İK / Birim -------------------------

    @GetMapping
    @PreAuthorize("hasAnyRole('HR','BIRIM')")
    @Operation(summary = "[İK/Birim] Başvuruları filtrele ve listele (sayfalı)",
            description = "İK gönderilmiş tüm başvuruları, birim sorumlusu yalnızca biriminize yönlendirilenleri görür.")
    public SayfaDto<BasvuruOzetDto> listele(
            @AuthenticationPrincipal GirisYapanKullanici k,
            @RequestParam(required = false) BasvuruDurumu durum,
            @RequestParam(required = false) Long departmanId,
            @RequestParam(required = false) String sehir,
            @RequestParam(required = false) StajTuru stajTuru,
            @Parameter(description = "Ad soyad, TC, okul veya alan içinde arar") @RequestParam(required = false) String arama,
            @PageableDefault(size = 20, sort = "guncellemeTarihi", direction = Sort.Direction.DESC) Pageable pageable) {
        return basvuruService.listele(k, new BasvuruService.Filtre(durum, departmanId, sehir, stajTuru, arama), pageable);
    }

    @GetMapping(value = "/disa-aktar", produces = "text/csv")
    @PreAuthorize("hasAnyRole('HR','BIRIM')")
    @Operation(summary = "[İK/Birim] Filtrelenmiş başvuruları CSV (Excel) olarak indir")
    public ResponseEntity<byte[]> disaAktar(
            @AuthenticationPrincipal GirisYapanKullanici k,
            @RequestParam(required = false) BasvuruDurumu durum,
            @RequestParam(required = false) Long departmanId,
            @RequestParam(required = false) String sehir,
            @RequestParam(required = false) StajTuru stajTuru,
            @RequestParam(required = false) String arama) {
        String csv = basvuruService.csvDisaAktar(k, new BasvuruService.Filtre(durum, departmanId, sehir, stajTuru, arama));
        String dosyaAdi = "staj-basvurulari-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(dosyaAdi, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @PutMapping("/{id}/durum")
    @PreAuthorize("hasAnyRole('HR','BIRIM')")
    @Operation(summary = "[İK/Birim] Başvuru durumunu değiştir",
            description = "İzinli geçişler iş akışına göre denetlenir. EKSIK_BELGE / REDDEDILDI için açıklama zorunludur; "
                    + "BIRIM_DEGERLENDIRMESINDE için departmanId (boşsa öğrencinin tercihi) kullanılır; ONAYLANDI kontenjan kontrolü yapar.")
    public BasvuruResponseDto durumGuncelle(@AuthenticationPrincipal GirisYapanKullanici k, @PathVariable Long id,
                                            @Valid @RequestBody BasvuruDurumGuncelleRequestDto dto) {
        return basvuruService.durumGuncelle(k, id, dto);
    }
}
