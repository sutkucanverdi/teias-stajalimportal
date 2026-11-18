package com.example.teiasbackend.repository;

import com.example.teiasbackend.entity.BelgeTuru;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BelgeTuruRepository extends JpaRepository<BelgeTuru, Long> {

    List<BelgeTuru> findAllByOrderByIdAsc();

    List<BelgeTuru> findByAktifTrueOrderByIdAsc();

    List<BelgeTuru> findByAktifTrueAndZorunluTrue();

    boolean existsByKodIgnoreCase(String kod);
}
