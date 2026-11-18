package com.example.teiasbackend.repository;

import com.example.teiasbackend.entity.BasvuruGecmisi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BasvuruGecmisiRepository extends JpaRepository<BasvuruGecmisi, Long> {

    List<BasvuruGecmisi> findByBasvuruIdOrderByIslemTarihiAscIdAsc(Long basvuruId);
}
