package com.example.teiasbackend.repository;

import com.example.teiasbackend.entity.Basvuru;
import com.example.teiasbackend.entity.BasvuruDurumu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BasvuruRepository extends JpaRepository<Basvuru, Long>, JpaSpecificationExecutor<Basvuru> {

    @EntityGraph(attributePaths = {"user", "tercihDepartman", "atananDepartman"})
    List<Basvuru> findByUserIdOrderByOlusturmaTarihiDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "tercihDepartman", "atananDepartman"})
    Optional<Basvuru> findWithDetayById(Long id);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user", "tercihDepartman", "atananDepartman"})
    Page<Basvuru> findAll(Specification<Basvuru> spec, @NonNull Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user", "tercihDepartman", "atananDepartman"})
    List<Basvuru> findAll(Specification<Basvuru> spec);

    boolean existsByUserIdAndDurumNotIn(Long userId, Collection<BasvuruDurumu> durumlar);

    long countByAtananDepartmanIdAndDurum(Long departmanId, BasvuruDurumu durum);

    long countByDurum(BasvuruDurumu durum);

    boolean existsByTercihDepartmanIdOrAtananDepartmanId(Long tercihId, Long atananId);

    @Query("select b.durum, count(b) from Basvuru b group by b.durum")
    List<Object[]> durumaGoreSayilar();

    @Query("""
            select d.id, d.ad, d.sehir, d.kontenjan,
                   sum(case when b.durum = com.example.teiasbackend.entity.BasvuruDurumu.ONAYLANDI then 1 else 0 end),
                   sum(case when b.durum = com.example.teiasbackend.entity.BasvuruDurumu.BIRIM_DEGERLENDIRMESINDE then 1 else 0 end)
            from Departman d left join Basvuru b on b.atananDepartman = d
            group by d.id, d.ad, d.sehir, d.kontenjan
            order by d.ad
            """)
    List<Object[]> departmanDoluluklari();
}
