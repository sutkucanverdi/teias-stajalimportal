package com.example.teiasbackend.repository;

import com.example.teiasbackend.entity.Departman;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmanRepository extends JpaRepository<Departman, Long> {

    List<Departman> findAllByOrderByAdAsc();

    List<Departman> findByAktifTrueOrderByAdAsc();

    boolean existsByAdIgnoreCase(String ad);

    boolean existsByAdIgnoreCaseAndIdNot(String ad, Long id);
}
