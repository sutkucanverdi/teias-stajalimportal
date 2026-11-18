-- TEİAŞ Lise Stajyer Başvuru Portalı - Başlangıç şeması

CREATE TABLE departmanlar (
    id            BIGSERIAL PRIMARY KEY,
    ad            VARCHAR(150)  NOT NULL UNIQUE,
    aciklama      TEXT,
    sehir         VARCHAR(60)   NOT NULL,
    kontenjan     INTEGER       NOT NULL DEFAULT 0,
    aktif         BOOLEAN       NOT NULL DEFAULT TRUE,
    olusturma_tarihi TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id               BIGSERIAL PRIMARY KEY,
    ad               VARCHAR(100) NOT NULL,
    soyad            VARCHAR(100) NOT NULL,
    tc_kimlik        VARCHAR(11)  UNIQUE,
    kullanici_adi    VARCHAR(100) NOT NULL UNIQUE,
    email            VARCHAR(150) UNIQUE,
    telefon          VARCHAR(20),
    dogum_tarihi     DATE,
    sifre            VARCHAR(100) NOT NULL,
    rol              VARCHAR(30)  NOT NULL,
    aktif_mi         BOOLEAN      NOT NULL DEFAULT TRUE,
    aktivasyon_kodu  VARCHAR(10),
    departman_id     BIGINT REFERENCES departmanlar(id) ON DELETE SET NULL,
    olusturma_tarihi TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE belge_turleri (
    id           BIGSERIAL PRIMARY KEY,
    kod          VARCHAR(40)  NOT NULL UNIQUE,
    ad           VARCHAR(150) NOT NULL,
    aciklama     TEXT,
    zorunlu      BOOLEAN      NOT NULL DEFAULT TRUE,
    max_boyut_mb INTEGER      NOT NULL DEFAULT 5,
    aktif        BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE basvurular (
    id                       BIGSERIAL PRIMARY KEY,
    user_id                  BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    okul_adi                 VARCHAR(200) NOT NULL,
    alan                     VARCHAR(120) NOT NULL,
    dal                      VARCHAR(120),
    sinif                    VARCHAR(10)  NOT NULL,
    not_ortalamasi           DOUBLE PRECISION,
    staj_turu                VARCHAR(20)  NOT NULL,
    sehir                    VARCHAR(60)  NOT NULL,
    baslama_tarihi           DATE         NOT NULL,
    bitis_tarihi             DATE         NOT NULL,
    ogrenci_notu             TEXT,
    tercih_departman_id      BIGINT REFERENCES departmanlar(id) ON DELETE SET NULL,
    atanan_departman_id      BIGINT REFERENCES departmanlar(id) ON DELETE SET NULL,
    durum                    VARCHAR(30)  NOT NULL,
    degerlendirme_notu       TEXT,
    basvuru_tarihi           TIMESTAMP,
    olusturma_tarihi         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    guncelleme_tarihi        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_basvurular_durum ON basvurular(durum);
CREATE INDEX idx_basvurular_user ON basvurular(user_id);
CREATE INDEX idx_basvurular_atanan_departman ON basvurular(atanan_departman_id);

CREATE TABLE basvuru_belgeleri (
    id             BIGSERIAL PRIMARY KEY,
    basvuru_id     BIGINT       NOT NULL REFERENCES basvurular(id) ON DELETE CASCADE,
    belge_turu_id  BIGINT       NOT NULL REFERENCES belge_turleri(id),
    dosya_yolu     VARCHAR(500) NOT NULL,
    orijinal_ad    VARCHAR(255) NOT NULL,
    icerik_turu    VARCHAR(100),
    boyut          BIGINT       NOT NULL,
    durum          VARCHAR(20)  NOT NULL,
    yonetici_notu  VARCHAR(500),
    yukleme_tarihi TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_belgeler_basvuru ON basvuru_belgeleri(basvuru_id);

CREATE TABLE basvuru_gecmisi (
    id           BIGSERIAL PRIMARY KEY,
    basvuru_id   BIGINT      NOT NULL REFERENCES basvurular(id) ON DELETE CASCADE,
    eski_durum   VARCHAR(30),
    yeni_durum   VARCHAR(30) NOT NULL,
    aciklama     TEXT,
    islem_yapan  VARCHAR(200) NOT NULL,
    islem_tarihi TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_gecmis_basvuru ON basvuru_gecmisi(basvuru_id);
