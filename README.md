# TEİAŞ Lise Stajyer Başvuru ve Yönetim Portalı

Lise öğrencilerinin TEİAŞ birimlerine staj başvurusu yapmasını, İnsan Kaynakları'nın başvuruları incelemesini ve
birim sorumlularının stajyer kabul kararını vermesini sağlayan uçtan uca web uygulaması.

| Katman    | Teknoloji                                                                 |
|-----------|---------------------------------------------------------------------------|
| Frontend  | Angular 22 (standalone bileşenler, signals, reactive forms), nginx        |
| Backend   | Java 21, Spring Boot 4.1, Spring Security + JWT, Spring Data JPA, Flyway  |
| Veritabanı| PostgreSQL 17                                                             |
| Dokümantasyon | Swagger UI (springdoc-openapi)                                        |
| Çalıştırma| Docker Compose                                                            |

## Hızlı Başlangıç (Docker)

```bash
docker compose up -d --build
```

| Adres | Açıklama |
|-------|----------|
| http://localhost | Portal (Angular) |
| http://localhost/swagger-ui.html | API dokümantasyonu (Swagger UI) |
| http://localhost:8080/api | REST API (doğrudan) |
| http://localhost:8080/actuator/health | Sağlık kontrolü |

### Demo hesaplar

| Rol | Kullanıcı adı | Şifre |
|-----|---------------|-------|
| İnsan Kaynakları | `ik_admin` | `teias2026` |
| Birim Sorumlusu (Bilgi Teknolojileri) | `bt_birim` | `teias2026` |
| Öğrenci | `10000001440` (TC) | `Ogrenci2026` |

> Demo hesaplar `DEMO_VERI=false` ile kapatılabilir. Ayarlar için `.env.example` dosyasını `.env` olarak kopyalayın.

## İş Akışı

```
 Öğrenci                       İnsan Kaynakları                     Birim Sorumlusu
 ───────                       ────────────────                     ───────────────
 Kayıt ol (TC doğrulamalı)
 Başvuru formu  → TASLAK
 Belgeleri yükle (PDF/JPG/PNG)
 Gönder         → GONDERILDI ─► İncelemeye al → IK_INCELEMESINDE
                                Belgeleri onayla / reddet
          ◄──── EKSIK_BELGE ◄── Eksik belge bildir (açıklama zorunlu)
 Belgeyi düzelt, yeniden gönder
                                Birime yönlendir → BIRIM_DEGERLENDIRMESINDE ─► Onayla → ONAYLANDI (kontenjan kontrolü)
                                Reddet → REDDEDILDI                           Reddet → REDDEDILDI
                                                                ◄──────────── İK'ya iade → IK_INCELEMESINDE
```

Geçişler backend'de bir durum makinesi (`BasvuruIsAkisi`) ile denetlenir; her değişiklik başvuru geçmişine
(zaman çizelgesi) işlenir. Bir öğrencinin aynı anda yalnızca bir aktif başvurusu olabilir.

## Özellikler

**Öğrenci:** TC Kimlik No algoritma kontrollü kayıt · başvuru formu (şehre göre birim ve kalan kontenjan) · belge
yükleme/değiştirme/silme · zorunlu belge kontrolü · adım adım durum takibi · İK notlarını görme · profil ve şifre.

**İnsan Kaynakları:** gösterge paneli (durum dağılımı, birim doluluk oranları) · filtrelenebilir, aranabilir,
sayfalı başvuru listesi · Excel'e (CSV) aktarma · belge onay/ret · durum yönetimi · birim ve kontenjan yönetimi ·
belge türü yönetimi · personel hesabı açma, kullanıcı aktif/pasif yapma.

**Birim sorumlusu:** yalnızca kendi birimine yönlendirilen başvuruları görme, belgeleri inceleme, onay/ret/İK'ya iade.

**Güvenlik:** BCrypt şifreleme · stateless JWT · rol bazlı yetkilendirme (`@PreAuthorize`) + kayıt bazlı erişim
kontrolü (öğrenci yalnızca kendi başvurusunu, birim yalnızca kendisine atananı görür) · dosya türü uzantı **ve**
içerik imzası (magic bytes) ile doğrulanır · path traversal koruması · CSV formül enjeksiyonu koruması · merkezi
hata yönetimi (tüm hatalar aynı JSON formatında).

## API Özeti

Tüm uç noktalar `/api` altındadır; ayrıntılar ve deneme için Swagger UI'yı kullanın.

| Metot | Yol | Yetki | Açıklama |
|-------|-----|-------|----------|
| POST | `/auth/register` | Herkes | Öğrenci kaydı |
| POST | `/auth/login` | Herkes | Giriş, JWT döner |
| POST | `/auth/aktivasyon` | Herkes | Hesap aktivasyonu (opsiyonel akış) |
| GET/PUT | `/auth/me` | Oturum | Profil bilgisi / iletişim güncelleme |
| PUT | `/auth/sifre` | Oturum | Şifre değiştirme |
| GET | `/departmanlar`, `/departmanlar/{id}` | Herkes | Birimler ve kalan kontenjan |
| POST/PUT/DELETE | `/departmanlar/{id?}` | İK | Birim yönetimi |
| GET | `/belge-turleri` | Herkes | İstenen belgeler |
| POST/PUT/DELETE | `/belge-turleri/{id?}` | İK | Belge türü yönetimi |
| GET | `/basvurular/benim` | Öğrenci | Başvurularım |
| POST | `/basvurular` | Öğrenci | Taslak başvuru oluştur |
| PUT | `/basvurular/{id}` | Öğrenci | Taslak / eksik belge durumunda güncelle |
| POST | `/basvurular/{id}/belgeler` | Öğrenci | Belge yükle (multipart: `belgeTuruId`, `dosya`) |
| DELETE | `/basvurular/{id}/belgeler/{belgeId}` | Öğrenci | Belge sil |
| POST | `/basvurular/{id}/gonder` | Öğrenci | İK'ya gönder |
| GET | `/basvurular/{id}`, `/basvurular/{id}/gecmis` | İlgili kişi | Detay ve zaman çizelgesi |
| GET | `/basvurular?durum=&departmanId=&sehir=&stajTuru=&arama=&page=&size=` | İK / Birim | Filtreli sayfalı liste |
| GET | `/basvurular/disa-aktar` | İK / Birim | CSV dışa aktarım |
| PUT | `/basvurular/{id}/durum` | İK / Birim | Durum değiştir (`durum`, `aciklama`, `departmanId`) |
| DELETE | `/basvurular/{id}` | Öğrenci (taslak) / İK | Başvuru sil |
| GET | `/belgeler/{id}/indir` | İlgili kişi | Belgeyi görüntüle / indir |
| PUT | `/belgeler/{id}/durum` | İK | Belgeyi onayla / reddet |
| GET/POST | `/users` | İK | Kullanıcı listesi / personel hesabı aç |
| PUT | `/users/{id}/aktiflik` | İK | Aktif / pasif |
| GET | `/istatistikler` | İK | Gösterge paneli verileri |

## Geliştirme Ortamı (Docker'sız)

```bash
# 1) Yalnızca veritabanını başlat (localhost:5434)
docker compose up -d db

# 2) Backend (http://localhost:8080)
cd backend
DB_URL=jdbc:postgresql://localhost:5434/teias_portal_db mvn spring-boot:run

# 3) Frontend (http://localhost:4200, /api istekleri proxy.conf.json ile 8080'e gider)
cd frontend
npm install
npm start
```

## Testler

```bash
cd backend && mvn test            # birim testleri (TC doğrulama, iş akışı kuralları)
cd frontend && npx ng test --watch=false
node scripts/api-smoke-test.mjs http://localhost    # çalışan sisteme karşı uçtan uca 45 adımlık API testi
```

## Proje Yapısı

```
backend/                 Spring Boot REST API
  src/main/java/com/example/teiasbackend/
    controller/          REST uç noktaları
    service/             iş kuralları (BasvuruIsAkisi = durum makinesi)
    entity/ repository/  JPA modelleri ve sorgular
    dto/                 istek / cevap nesneleri (validasyonlu record'lar)
    security/            JWT filtresi, SecurityConfig
    validation/          @TcKimlikNo doğrulayıcı
    exception/           merkezi hata yönetimi
  src/main/resources/db/migration/   Flyway şema ve başlangıç verileri
frontend/                Angular uygulaması
  src/app/core/          API servisi, kimlik doğrulama, guard, interceptor, modeller
  src/app/layout/        kurumsal çerçeve (üst bant, menü, alt bilgi)
  src/app/pages/         öğrenci, İK, birim ve ortak sayfalar
  nginx.conf             SPA + /api ters vekil
scripts/api-smoke-test.mjs   uçtan uca API testi
docker-compose.yml
```
