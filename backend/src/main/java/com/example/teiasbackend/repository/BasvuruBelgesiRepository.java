package com.example.teiasbackend.repository;

import com.example.teiasbackend.entity.BasvuruBelgesi;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BasvuruBelgesiRepository extends JpaRepository<BasvuruBelgesi, Long> {

    @EntityGraph(attributePaths = {"belgeTuru"})
    List<BasvuruBelgesi> findByBasvuruIdOrderByYuklemeTarihiAsc(Long basvuruId);

    @EntityGraph(attributePaths = {"belgeTuru", "basvuru", "basvuru.user", "basvuru.atananDepartman"})
    Optional<BasvuruBelgesi> findWithDetayById(Long id);

    boolean existsByBelgeTuruId(Long belgeTuruId);
}
