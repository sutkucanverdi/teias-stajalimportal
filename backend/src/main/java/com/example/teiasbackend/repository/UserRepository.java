package com.example.teiasbackend.repository;

import com.example.teiasbackend.entity.Rol;
import com.example.teiasbackend.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "departman")
    Optional<User> findByKullaniciAdi(String kullaniciAdi);

    @EntityGraph(attributePaths = "departman")
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByTcKimlik(String tcKimlik);

    boolean existsByTcKimlik(String tcKimlik);

    boolean existsByKullaniciAdi(String kullaniciAdi);

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "departman")
    List<User> findByRolOrderByAdAsc(Rol rol);

    @EntityGraph(attributePaths = "departman")
    List<User> findAllByOrderByOlusturmaTarihiDesc();

    long countByRol(Rol rol);
}
