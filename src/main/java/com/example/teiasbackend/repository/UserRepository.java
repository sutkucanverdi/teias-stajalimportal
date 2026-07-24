package com.example.teiasbackend.repository;

import com.example.teiasbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKullaniciAdi(String kullaniciAdi);

    Optional<User> findByTcKimlik(String tcKimlik);
}