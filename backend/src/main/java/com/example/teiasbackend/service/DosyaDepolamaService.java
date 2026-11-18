package com.example.teiasbackend.service;

import com.example.teiasbackend.exception.IsKuraliException;
import com.example.teiasbackend.exception.KayitBulunamadiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Yüklenen belgeleri disk üzerinde saklar. Dosya türü uzantı + içerik imzası (magic bytes) ile doğrulanır. */
@Slf4j
@Service
public class DosyaDepolamaService {

    /** İzin verilen uzantı -> MIME türü */
    private static final Map<String, String> IZINLI_TURLER = Map.of(
            "pdf", "application/pdf",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png"
    );

    private final Path kokDizin;

    public DosyaDepolamaService(@Value("${app.dosya.dizin}") String dizin) throws IOException {
        this.kokDizin = Path.of(dizin).toAbsolutePath().normalize();
        Files.createDirectories(kokDizin);
        log.info("Belge depolama dizini: {}", kokDizin);
    }

    public record KayitliDosya(String goreliYol, String icerikTuru, long boyut) {
    }

    public KayitliDosya kaydet(MultipartFile dosya, String altKlasor, int maxBoyutMb) {
        if (dosya == null || dosya.isEmpty()) {
            throw new IsKuraliException("Lütfen bir dosya seçiniz.");
        }
        if (dosya.getSize() > (long) maxBoyutMb * 1024 * 1024) {
            throw new IsKuraliException("Dosya boyutu en fazla " + maxBoyutMb + " MB olabilir.");
        }
        String uzanti = uzantiAl(dosya.getOriginalFilename());
        String mime = IZINLI_TURLER.get(uzanti);
        if (mime == null) {
            throw new IsKuraliException("Yalnızca PDF, JPG ve PNG formatındaki dosyalar yüklenebilir.");
        }
        try {
            if (!imzaUyumluMu(dosya, uzanti)) {
                throw new IsKuraliException("Dosya içeriği uzantısıyla uyuşmuyor. Lütfen geçerli bir " + uzanti.toUpperCase(Locale.ROOT) + " dosyası yükleyiniz.");
            }
            Path klasor = kokDizin.resolve(altKlasor).normalize();
            if (!klasor.startsWith(kokDizin)) {
                throw new IsKuraliException("Geçersiz depolama yolu.");
            }
            Files.createDirectories(klasor);
            String dosyaAdi = UUID.randomUUID() + "." + uzanti;
            Path hedef = klasor.resolve(dosyaAdi);
            try (InputStream in = dosya.getInputStream()) {
                Files.copy(in, hedef, StandardCopyOption.REPLACE_EXISTING);
            }
            return new KayitliDosya(kokDizin.relativize(hedef).toString().replace('\\', '/'), mime, dosya.getSize());
        } catch (IOException e) {
            log.error("Dosya kaydedilemedi", e);
            throw new IsKuraliException("Dosya kaydedilemedi, lütfen tekrar deneyiniz.");
        }
    }

    public Resource oku(String goreliYol) {
        try {
            Path yol = kokDizin.resolve(goreliYol).normalize();
            if (!yol.startsWith(kokDizin) || !Files.isReadable(yol)) {
                throw new KayitBulunamadiException("Belge dosyası sunucuda bulunamadı.");
            }
            return new UrlResource(yol.toUri());
        } catch (MalformedURLException e) {
            throw new KayitBulunamadiException("Belge dosyası okunamadı.");
        }
    }

    public void sil(String goreliYol) {
        try {
            Path yol = kokDizin.resolve(goreliYol).normalize();
            if (yol.startsWith(kokDizin)) {
                Files.deleteIfExists(yol);
            }
        } catch (IOException e) {
            log.warn("Dosya silinemedi: {}", goreliYol, e);
        }
    }

    private static String uzantiAl(String ad) {
        if (ad == null || !ad.contains(".")) return "";
        return ad.substring(ad.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean imzaUyumluMu(MultipartFile dosya, String uzanti) throws IOException {
        byte[] bas = new byte[8];
        int okunan;
        try (InputStream in = dosya.getInputStream()) {
            okunan = in.readNBytes(bas, 0, bas.length);
        }
        byte[] b = Arrays.copyOf(bas, okunan);
        return switch (uzanti) {
            case "pdf" -> baslar(b, new byte[]{'%', 'P', 'D', 'F'});
            case "png" -> baslar(b, new byte[]{(byte) 0x89, 'P', 'N', 'G'});
            case "jpg", "jpeg" -> baslar(b, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
            default -> false;
        };
    }

    private static boolean baslar(byte[] veri, byte[] imza) {
        if (veri.length < imza.length) return false;
        for (int i = 0; i < imza.length; i++) {
            if (veri[i] != imza[i]) return false;
        }
        return true;
    }
}
