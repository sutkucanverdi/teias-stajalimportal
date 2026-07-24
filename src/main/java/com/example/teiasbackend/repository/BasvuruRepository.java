package com.example.teiasbackend.repository;

import com.example.teiasbackend.entity.Basvuru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BasvuruRepository extends JpaRepository<Basvuru, Long> {

    List<Basvuru> findByDurum(String durum);
    List<Basvuru> findByDepartman(String departman);

    @Query("SELECT b FROM Basvuru b WHERE " +
            "(:durum IS NULL OR b.durum = :durum) AND " +
            "(:departman IS NULL OR b.departman = :departman) AND " +
            "(:sehir IS NULL OR b.sehir = :sehir)")
    List<Basvuru> cokluFiltrele(@Param("durum") String durum,
                                @Param("departman") String departman,
                                @Param("sehir") String sehir);
}