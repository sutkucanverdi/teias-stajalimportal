package com.example.teiasbackend.repository;

import com.example.teiasbackend.entity.Basvuru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BasvuruRepository extends JpaRepository<Basvuru, Long> {
    // Spring Data JPA bizim için tüm kayıt (save), silme (delete) ve bulma (findById) komutlarını arka planda otomatik yazdı!
}