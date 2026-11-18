package com.example.teiasbackend.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Sayfalı liste cevabı */
public record SayfaDto<T>(
        List<T> icerik,
        int sayfa,
        int boyut,
        long toplamKayit,
        int toplamSayfa
) {
    public static <E, T> SayfaDto<T> from(Page<E> page, Function<E, T> donustur) {
        return new SayfaDto<>(page.getContent().stream().map(donustur).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
