package com.example.teiasbackend.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Merkezi hata yönetimi: tüm hatalar {@link ApiHata} formatında döner. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(KayitBulunamadiException.class)
    public ResponseEntity<ApiHata> bulunamadi(KayitBulunamadiException ex, HttpServletRequest req) {
        return cevap(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(IsKuraliException.class)
    public ResponseEntity<ApiHata> isKurali(IsKuraliException ex, HttpServletRequest req) {
        return cevap(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(CakismaException.class)
    public ResponseEntity<ApiHata> cakisma(CakismaException ex, HttpServletRequest req) {
        return cevap(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler({YetkisizIslemException.class, AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ApiHata> yetkisiz(RuntimeException ex, HttpServletRequest req) {
        String mesaj = ex instanceof YetkisizIslemException ? ex.getMessage() : "Bu işlem için yetkiniz bulunmamaktadır.";
        return cevap(HttpStatus.FORBIDDEN, mesaj, req);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiHata> hataliGiris(BadCredentialsException ex, HttpServletRequest req) {
        return cevap(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiHata> pasifHesap(DisabledException ex, HttpServletRequest req) {
        return cevap(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiHata> dogrulama(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> alanlar = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> alanlar.putIfAbsent(e.getField(), e.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors()
                .forEach(e -> alanlar.putIfAbsent(e.getObjectName(), e.getDefaultMessage()));
        ApiHata hata = new ApiHata(LocalDateTime.now(), 400, "Bad Request",
                "Gönderilen bilgilerde hatalar var. Lütfen kontrol ediniz.", req.getRequestURI(), alanlar);
        return ResponseEntity.badRequest().body(hata);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiHata> kisit(ConstraintViolationException ex, HttpServletRequest req) {
        return cevap(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    public ResponseEntity<ApiHata> okunamayanIstek(Exception ex, HttpServletRequest req) {
        return cevap(HttpStatus.BAD_REQUEST, "İstek gövdesi veya parametreleri hatalı: " + kisalt(ex.getMessage()), req);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiHata> dosyaBuyuk(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return cevap(HttpStatus.PAYLOAD_TOO_LARGE, "Yüklenen dosya izin verilen en büyük boyutu (10 MB) aşıyor.", req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiHata> veriButunlugu(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Veri bütünlüğü ihlali: {}", ex.getMostSpecificCause().getMessage());
        return cevap(HttpStatus.CONFLICT, "Kayıt veritabanı kısıtlarıyla çakışıyor (aynı bilgilerle kayıt mevcut olabilir).", req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiHata> kaynakYok(NoResourceFoundException ex, HttpServletRequest req) {
        return cevap(HttpStatus.NOT_FOUND, "İstenen adres bulunamadı.", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiHata> beklenmeyen(Exception ex, HttpServletRequest req) {
        log.error("Beklenmeyen hata [{} {}]", req.getMethod(), req.getRequestURI(), ex);
        return cevap(HttpStatus.INTERNAL_SERVER_ERROR, "Sunucuda beklenmeyen bir hata oluştu. Lütfen daha sonra tekrar deneyiniz.", req);
    }

    private ResponseEntity<ApiHata> cevap(HttpStatus durum, String mesaj, HttpServletRequest req) {
        return ResponseEntity.status(durum)
                .body(ApiHata.of(durum.value(), durum.getReasonPhrase(), mesaj, req.getRequestURI()));
    }

    private static String kisalt(String s) {
        if (s == null) return "";
        return s.length() > 160 ? s.substring(0, 160) + "..." : s;
    }
}
