// TEİAŞ Staj Portalı - Uçtan uca API testi (Node 18+)
// Kullanım:  node scripts/api-smoke-test.mjs [http://localhost:8080]
// Tüm iş akışını gerçek API üzerinde dener: kayıt → başvuru → belge → İK → birim → onay.

const API = (process.argv[2] || process.env.API_URL || 'http://localhost:8080').replace(/\/$/, '') + '/api';
let gecen = 0;
let kalan = 0;

function kontrol(kosul, mesaj, detay) {
  if (kosul) {
    gecen++;
    console.log('  ✔ ' + mesaj);
  } else {
    kalan++;
    console.log('  ✘ ' + mesaj + (detay ? ' -> ' + JSON.stringify(detay).slice(0, 300) : ''));
  }
}

async function istek(method, yol, { token, json, form } = {}) {
  const headers = {};
  if (token) headers.Authorization = 'Bearer ' + token;
  let body;
  if (json !== undefined) {
    headers['Content-Type'] = 'application/json';
    body = JSON.stringify(json);
  } else if (form) {
    body = form;
  }
  const res = await fetch(API + yol, { method, headers, body });
  const tip = res.headers.get('content-type') || '';
  const veri = tip.includes('json') ? await res.json() : await res.text();
  return { status: res.status, veri, headers: res.headers };
}

/** Resmi algoritmaya uygun rastgele TC Kimlik No üretir */
function rastgeleTc() {
  const d = [1 + Math.floor(Math.random() * 9)];
  for (let i = 1; i < 9; i++) d.push(Math.floor(Math.random() * 10));
  const tek = d[0] + d[2] + d[4] + d[6] + d[8];
  const cift = d[1] + d[3] + d[5] + d[7];
  d.push((((tek * 7 - cift) % 10) + 10) % 10);
  d.push(d.reduce((a, b) => a + b, 0) % 10);
  return d.join('');
}

function pdf(ad) {
  const icerik = '%PDF-1.4\n1 0 obj<</Type/Catalog>>endobj\ntrailer<</Root 1 0 R>>\n%%EOF\n';
  const f = new FormData();
  f.append('dosya', new Blob([icerik], { type: 'application/pdf' }), ad);
  return f;
}

function gunSonra(n) {
  const t = new Date();
  t.setDate(t.getDate() + n);
  return t.toISOString().slice(0, 10);
}

async function main() {
  console.log('API: ' + API + '\n');

  console.log('1) Genel uç noktalar');
  const bilgi = await istek('GET', '/sistem/bilgi');
  kontrol(bilgi.status === 200, 'Sistem bilgisi erişilebilir');
  const departmanlar = (await istek('GET', '/departmanlar')).veri;
  kontrol(Array.isArray(departmanlar) && departmanlar.length > 0, 'Birimler listelendi (' + departmanlar.length + ')');
  const turler = (await istek('GET', '/belge-turleri')).veri;
  const zorunlular = turler.filter((t) => t.zorunlu);
  kontrol(zorunlular.length >= 1, 'Belge türleri listelendi (zorunlu: ' + zorunlular.length + ')');
  const yetkisiz = await istek('GET', '/basvurular/benim');
  kontrol(yetkisiz.status === 401, 'Token olmadan korumalı uç nokta 401 döner');

  console.log('\n2) Öğrenci kaydı ve giriş');
  const tc = rastgeleTc();
  const sifre = 'Deneme2026';
  const hataliKayit = await istek('POST', '/auth/register', {
    json: { tcKimlik: '12345678901', ad: 'X', soyad: 'Y', email: 'gecersiz', telefon: '123', dogumTarihi: '2009-01-01', sifre: 'kisa' },
  });
  kontrol(hataliKayit.status === 400 && hataliKayit.veri.alanHatalari?.tcKimlik, 'Geçersiz TC / alanlar 400 + alan hataları döner', hataliKayit.veri);
  const kayit = await istek('POST', '/auth/register', {
    json: { tcKimlik: tc, ad: 'Zeynep', soyad: 'Test', email: `zeynep.${tc}@ornek.com`, telefon: '05321234567', dogumTarihi: '2009-05-20', sifre },
  });
  kontrol(kayit.status === 201, 'Öğrenci kaydı oluşturuldu', kayit.veri);
  const tekrar = await istek('POST', '/auth/register', {
    json: { tcKimlik: tc, ad: 'Zeynep', soyad: 'Test', email: `baska.${tc}@ornek.com`, telefon: '05321234567', dogumTarihi: '2009-05-20', sifre },
  });
  kontrol(tekrar.status === 409, 'Aynı TC ile ikinci kayıt 409 döner');
  const yanlisSifre = await istek('POST', '/auth/login', { json: { kullaniciAdi: tc, sifre: 'yanlis123' } });
  kontrol(yanlisSifre.status === 401, 'Hatalı şifre 401 döner');
  const ogrGiris = await istek('POST', '/auth/login', { json: { kullaniciAdi: tc, sifre } });
  kontrol(ogrGiris.status === 200 && ogrGiris.veri.token, 'Öğrenci giriş yaptı');
  const ogr = ogrGiris.veri.token;
  const me = await istek('GET', '/auth/me', { token: ogr });
  kontrol(me.veri.rol === 'ROLE_STUDENT', 'Oturum bilgisi: ROLE_STUDENT');
  kontrol((await istek('GET', '/users', { token: ogr })).status === 403, 'Öğrenci İK uç noktasına erişemez (403)');

  console.log('\n3) Başvuru oluşturma ve belge yükleme');
  const bt = departmanlar.find((d) => d.ad.startsWith('Bilgi Teknolojileri')) || departmanlar[0];
  const basvuruGovde = {
    okulAdi: 'Ankara Mesleki ve Teknik Anadolu Lisesi', alan: 'Bilişim Teknolojileri', dal: 'Yazılım Geliştirme',
    sinif: '11', notOrtalamasi: 87.5, stajTuru: 'ZORUNLU', sehir: 'Ankara', tercihDepartmanId: bt.id,
    baslamaTarihi: gunSonra(30), bitisTarihi: gunSonra(60), ogrenciNotu: 'Yazılım ekibinde staj yapmak istiyorum.',
  };
  const tarihHatasi = await istek('POST', '/basvurular', { token: ogr, json: { ...basvuruGovde, bitisTarihi: gunSonra(10) } });
  kontrol(tarihHatasi.status === 400, 'Bitiş < başlangıç tarihi 400 döner');
  const olustur = await istek('POST', '/basvurular', { token: ogr, json: basvuruGovde });
  kontrol(olustur.status === 201 && olustur.veri.durum === 'TASLAK', 'Taslak başvuru oluşturuldu', olustur.veri);
  const id = olustur.veri.id;
  kontrol((await istek('POST', '/basvurular', { token: ogr, json: basvuruGovde })).status === 400, 'İkinci aktif başvuru engellendi');
  const erkenGonder = await istek('POST', `/basvurular/${id}/gonder`, { token: ogr });
  kontrol(erkenGonder.status === 400 && /zorunlu belge/.test(erkenGonder.veri.mesaj), 'Belgesiz gönderim engellendi');

  const sahte = new FormData();
  sahte.append('dosya', new Blob(['bu bir pdf degil'], { type: 'application/pdf' }), 'sahte.pdf');
  const sahteYukle = await istek('POST', `/basvurular/${id}/belgeler?belgeTuruId=${zorunlular[0].id}`, { token: ogr, form: sahte });
  kontrol(sahteYukle.status === 400, 'İçeriği PDF olmayan dosya reddedildi');
  const belgeIdleri = {};
  for (const t of zorunlular) {
    const y = await istek('POST', `/basvurular/${id}/belgeler?belgeTuruId=${t.id}`, { token: ogr, form: pdf(t.kod.toLowerCase() + '.pdf') });
    kontrol(y.status === 201, `${t.ad} yüklendi`, y.veri);
    belgeIdleri[t.kod] = y.veri.id;
  }
  const gonder = await istek('POST', `/basvurular/${id}/gonder`, { token: ogr });
  kontrol(gonder.status === 200 && gonder.veri.durum === 'GONDERILDI', 'Başvuru İK\'ya gönderildi', gonder.veri);
  kontrol((await istek('PUT', `/basvurular/${id}`, { token: ogr, json: basvuruGovde })).status === 400, 'Gönderilmiş başvuru düzenlenemez');

  console.log('\n4) İK değerlendirmesi');
  const ikGiris = await istek('POST', '/auth/login', { json: { kullaniciAdi: 'ik_admin', sifre: 'teias2026' } });
  kontrol(ikGiris.status === 200, 'İK yöneticisi giriş yaptı');
  const ik = ikGiris.veri.token;
  const liste = await istek('GET', '/basvurular?durum=GONDERILDI&arama=zeynep', { token: ik });
  kontrol(liste.status === 200 && liste.veri.icerik.some((b) => b.id === id), 'İK listesinde başvuru görünüyor (filtre + arama)');
  const incele = await istek('PUT', `/basvurular/${id}/durum`, { token: ik, json: { durum: 'IK_INCELEMESINDE' } });
  kontrol(incele.veri.durum === 'IK_INCELEMESINDE', 'İncelemeye alındı');
  const gecersizGecis = await istek('PUT', `/basvurular/${id}/durum`, { token: ik, json: { durum: 'TASLAK' } });
  kontrol(gecersizGecis.status === 400, 'İzinsiz durum geçişi engellendi');
  const ilkKod = zorunlular[0].kod;
  const retNotsuz = await istek('PUT', `/belgeler/${belgeIdleri[ilkKod]}/durum`, { token: ik, json: { durum: 'REDDEDILDI' } });
  kontrol(retNotsuz.status === 400, 'Açıklamasız belge reddi engellendi');
  const ret = await istek('PUT', `/belgeler/${belgeIdleri[ilkKod]}/durum`, { token: ik, json: { durum: 'REDDEDILDI', not: 'Belge okunmuyor, tekrar yükleyiniz.' } });
  kontrol(ret.veri.durum === 'REDDEDILDI', 'Bir belge reddedildi');
  const eksik = await istek('PUT', `/basvurular/${id}/durum`, { token: ik, json: { durum: 'EKSIK_BELGE', aciklama: 'İSG belgenizi yeniden yükleyiniz.' } });
  kontrol(eksik.veri.durum === 'EKSIK_BELGE', 'Eksik belge talep edildi');

  console.log('\n5) Öğrenci eksik belgeyi tamamlar');
  const tekrarGonderErken = await istek('POST', `/basvurular/${id}/gonder`, { token: ogr });
  kontrol(tekrarGonderErken.status === 400, 'Reddedilen belge değiştirilmeden gönderilemez');
  const yeniden = await istek('POST', `/basvurular/${id}/belgeler?belgeTuruId=${zorunlular[0].id}`, { token: ogr, form: pdf('yeni-isg.pdf') });
  kontrol(yeniden.status === 201 && yeniden.veri.durum === 'BEKLIYOR', 'Belge yeniden yüklendi');
  belgeIdleri[ilkKod] = yeniden.veri.id;
  const tekrarGonder = await istek('POST', `/basvurular/${id}/gonder`, { token: ogr });
  kontrol(tekrarGonder.veri.durum === 'GONDERILDI', 'Başvuru yeniden gönderildi');

  console.log('\n6) İK onay ve birime yönlendirme');
  for (const kod of Object.keys(belgeIdleri)) {
    await istek('PUT', `/belgeler/${belgeIdleri[kod]}/durum`, { token: ik, json: { durum: 'ONAYLANDI' } });
  }
  const detay = await istek('GET', `/basvurular/${id}`, { token: ik });
  kontrol(detay.veri.belgeler.every((b) => b.durum === 'ONAYLANDI'), 'Tüm belgeler onaylandı');
  kontrol(detay.veri.yapilabilecekGecisler.includes('BIRIM_DEGERLENDIRMESINDE'), 'Detayda yapılabilecek geçişler dönüyor');
  const yonlendir = await istek('PUT', `/basvurular/${id}/durum`, { token: ik, json: { durum: 'BIRIM_DEGERLENDIRMESINDE', departmanId: bt.id } });
  kontrol(yonlendir.veri.durum === 'BIRIM_DEGERLENDIRMESINDE' && yonlendir.veri.atananDepartman?.id === bt.id, 'Birime yönlendirildi');

  console.log('\n7) Birim sorumlusu kararı');
  const birimGiris = await istek('POST', '/auth/login', { json: { kullaniciAdi: 'bt_birim', sifre: 'teias2026' } });
  kontrol(birimGiris.status === 200, 'Birim sorumlusu giriş yaptı');
  const birim = birimGiris.veri.token;
  const birimListe = await istek('GET', '/basvurular', { token: birim });
  kontrol(birimListe.veri.icerik.some((b) => b.id === id), 'Birim listesinde yönlendirilen başvuru var');
  kontrol((await istek('PUT', `/belgeler/${belgeIdleri[ilkKod]}/durum`, { token: birim, json: { durum: 'ONAYLANDI' } })).status === 403, 'Birim belge değerlendiremez (403)');
  const indirBirim = await istek('GET', `/belgeler/${belgeIdleri[ilkKod]}/indir`, { token: birim });
  kontrol(indirBirim.status === 200, 'Birim, kendisine atanan başvurunun belgesini indirebilir');
  const onay = await istek('PUT', `/basvurular/${id}/durum`, { token: birim, json: { durum: 'ONAYLANDI' } });
  kontrol(onay.veri.durum === 'ONAYLANDI', 'Başvuru ONAYLANDI', onay.veri);

  console.log('\n8) Sonuç, geçmiş ve raporlar');
  const benim = await istek('GET', '/basvurular/benim', { token: ogr });
  kontrol(benim.veri[0].durum === 'ONAYLANDI' && benim.veri[0].degerlendirmeNotu, 'Öğrenci sonucu ve açıklamayı görüyor');
  const gecmis = await istek('GET', `/basvurular/${id}/gecmis`, { token: ogr });
  const adimlar = gecmis.veri.map((g) => g.yeniDurum).join(' → ');
  kontrol(gecmis.veri.length >= 7, 'Zaman çizelgesi: ' + adimlar);
  const indir = await istek('GET', `/belgeler/${belgeIdleri[ilkKod]}/indir`, { token: ogr });
  kontrol(indir.status === 200 && String(indir.veri).startsWith('%PDF'), 'Öğrenci kendi belgesini indirebiliyor');
  const ist = await istek('GET', '/istatistikler', { token: ik });
  kontrol(ist.status === 200 && ist.veri.onaylanan >= 1, `İstatistikler (toplam: ${ist.veri.toplamBasvuru}, onaylanan: ${ist.veri.onaylanan})`);
  const csv = await istek('GET', '/basvurular/disa-aktar', { token: ik });
  kontrol(csv.status === 200 && String(csv.veri).includes('Başvuru No'), 'CSV dışa aktarım çalışıyor');
  const departman = (await istek('GET', `/departmanlar/${bt.id}`)).veri;
  kontrol(departman.onaylananSayisi >= 1, `Birim kontenjanı güncellendi (${departman.onaylananSayisi}/${departman.kontenjan})`);

  console.log(`\nSONUÇ: ${gecen} başarılı, ${kalan} başarısız`);
  process.exit(kalan === 0 ? 0 : 1);
}

main().catch((e) => {
  console.error('Test çalıştırılamadı:', e);
  process.exit(2);
});
