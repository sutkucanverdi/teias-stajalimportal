package com.example.teiasbackend.security;

import com.example.teiasbackend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

/** JWT (yaka kartı) üretim ve doğrulama servisi. */
@Service
public class JwtService {

    private final SecretKey anahtar;
    private final long gecerlilikDakika;

    public JwtService(@Value("${app.jwt.secret}") String gizliAnahtar,
                      @Value("${app.jwt.gecerlilik-dakika}") long gecerlilikDakika) {
        byte[] bytes = gizliAnahtar.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret en az 32 karakter olmalıdır.");
        }
        this.anahtar = Keys.hmacShaKeyFor(bytes);
        this.gecerlilikDakika = gecerlilikDakika;
    }

    public String tokenUret(User user) {
        Instant simdi = Instant.now();
        return Jwts.builder()
                .subject(user.getKullaniciAdi())
                .claim("uid", user.getId())
                .claim("rol", user.getRol().name())
                .claim("ad", user.getAdSoyad())
                .issuer("teias-staj-portali")
                .issuedAt(Date.from(simdi))
                .expiration(Date.from(simdi.plus(gecerlilikDakika, ChronoUnit.MINUTES)))
                .signWith(anahtar)
                .compact();
    }

    public long getGecerlilikSaniye() {
        return gecerlilikDakika * 60;
    }

    /** Token geçerliyse içindeki kullanıcı adını döner. */
    public Optional<String> kullaniciAdiCoz(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(anahtar)
                    .requireIssuer("teias-staj-portali")
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
