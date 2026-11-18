package com.example.teiasbackend.dto;

import java.util.List;
import java.util.Map;

public record IstatistikDto(
        long toplamBasvuru,
        long toplamOgrenci,
        long bekleyenIkIslemi,
        long birimdeBekleyen,
        long onaylanan,
        long reddedilen,
        Map<String, Long> durumaGore,
        List<DepartmanDoluluk> departmanlar
) {
    public record DepartmanDoluluk(Long id, String ad, String sehir, int kontenjan,
                                   long onaylanan, long degerlendirmede, int dolulukYuzdesi) {
    }
}
