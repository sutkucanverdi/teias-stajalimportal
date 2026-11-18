-- Evrak türleri (lise staj başvurusu için)
INSERT INTO belge_turleri (kod, ad, aciklama, zorunlu, max_boyut_mb) VALUES
 ('ISG',        'İş Sağlığı ve Güvenliği Eğitim Belgesi', 'Okulunuzdan veya e-Devlet üzerinden alınan İSG eğitim sertifikası.', TRUE, 5),
 ('OKUL_KABUL', 'Okul Staj Kabul / İşletme Formu',        'Okul müdürlüğü tarafından onaylanmış işletmede beceri eğitimi formu.', TRUE, 5),
 ('VELI_IZIN',  'Veli İzin Belgesi',                      '18 yaşından küçük öğrenciler için imzalı veli muvafakatnamesi.', TRUE, 5),
 ('OGRENCI_BELGESI', 'Öğrenci Belgesi',                   'e-Okul veya e-Devlet üzerinden alınmış güncel öğrenci belgesi.', FALSE, 5);

-- TEİAŞ birimleri (örnek)
INSERT INTO departmanlar (ad, aciklama, sehir, kontenjan) VALUES
 ('Bilgi Teknolojileri Dairesi Başkanlığı', 'Yazılım, ağ ve sistem altyapısı birimleri. Bilişim Teknolojileri alanı öğrencileri için uygundur.', 'Ankara', 10),
 ('İletim Tesis ve İşletme Dairesi Başkanlığı', 'Trafo merkezi işletme ve bakım süreçleri. Elektrik-Elektronik alanı öğrencileri için uygundur.', 'Ankara', 8),
 ('Yük Tevzi Dairesi Başkanlığı', 'Ulusal elektrik sisteminin gerçek zamanlı izlenmesi ve kontrolü.', 'Ankara', 4),
 ('1. Bölge Müdürlüğü - Trafo Merkezleri Bakım', 'İstanbul Avrupa yakası trafo merkezleri bakım ve onarım ekipleri.', 'İstanbul', 12),
 ('3. Bölge Müdürlüğü - Elektrik Bakım', 'İzmir ve çevresi iletim hatları bakım birimleri.', 'İzmir', 6),
 ('Destek Hizmetleri Dairesi Başkanlığı', 'İdari işler, büro yönetimi ve muhasebe süreçleri.', 'Ankara', 5);
