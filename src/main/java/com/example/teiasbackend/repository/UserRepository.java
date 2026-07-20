package com.example.teiasbackend.repository;

import com.example.teiasbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository<Hangi Tabloyu Yönetecek, ID veri tipi ne>
public interface UserRepository extends JpaRepository<User, Long> {

    // SİHİR BURADA: Biz buraya sadece metodun adını yazıyoruz,
    // Spring Boot arka planda SQL sorgusunu ("SELECT * FROM users WHERE tc_kimlik = ?") otomatik yazıyor!
    User findByTcKimlik(String tcKimlik);
}