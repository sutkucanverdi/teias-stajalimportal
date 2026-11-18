export type Rol = 'ROLE_STUDENT' | 'ROLE_HR' | 'ROLE_BIRIM';

export type BasvuruDurumu =
  | 'TASLAK'
  | 'GONDERILDI'
  | 'IK_INCELEMESINDE'
  | 'EKSIK_BELGE'
  | 'BIRIM_DEGERLENDIRMESINDE'
  | 'ONAYLANDI'
  | 'REDDEDILDI';

export type BelgeDurumu = 'BEKLIYOR' | 'ONAYLANDI' | 'REDDEDILDI';
export type StajTuru = 'ZORUNLU' | 'GONULLU';

export const DURUM_ETIKETLERI: Record<BasvuruDurumu, string> = {
  TASLAK: 'Taslak',
  GONDERILDI: 'Gönderildi',
  IK_INCELEMESINDE: 'İK İncelemesinde',
  EKSIK_BELGE: 'Eksik Belge',
  BIRIM_DEGERLENDIRMESINDE: 'Birim Değerlendirmesinde',
  ONAYLANDI: 'Onaylandı',
  REDDEDILDI: 'Reddedildi',
};

/** Durum değiştirme butonlarında gösterilecek eylem adları */
export const GECIS_ETIKETLERI: Record<BasvuruDurumu, string> = {
  TASLAK: 'Taslağa Al',
  GONDERILDI: 'Gönder',
  IK_INCELEMESINDE: 'İncelemeye Al',
  EKSIK_BELGE: 'Eksik Belge Bildir',
  BIRIM_DEGERLENDIRMESINDE: 'Birime Yönlendir',
  ONAYLANDI: 'Onayla',
  REDDEDILDI: 'Reddet',
};

export const BELGE_DURUM_ETIKETLERI: Record<BelgeDurumu, string> = {
  BEKLIYOR: 'İnceleme Bekliyor',
  ONAYLANDI: 'Onaylandı',
  REDDEDILDI: 'Reddedildi',
};

export const STAJ_TURU_ETIKETLERI: Record<StajTuru, string> = {
  ZORUNLU: 'Zorunlu Staj (İşletmede Beceri Eğitimi)',
  GONULLU: 'Gönüllü Staj',
};

export const ROL_ETIKETLERI: Record<Rol, string> = {
  ROLE_STUDENT: 'Öğrenci',
  ROLE_HR: 'İnsan Kaynakları',
  ROLE_BIRIM: 'Birim Sorumlusu',
};

export interface Kullanici {
  id: number;
  ad: string;
  soyad: string;
  tcKimlik: string | null;
  kullaniciAdi: string;
  email: string | null;
  telefon: string | null;
  dogumTarihi: string | null;
  rol: Rol;
  aktifMi: boolean;
  departmanId: number | null;
  departmanAdi: string | null;
  olusturmaTarihi: string;
}

export interface LoginCevap {
  mesaj: string;
  token: string;
  tokenTuru: string;
  gecerlilikSaniye: number;
  kullanici: Kullanici;
}

export interface Mesaj {
  mesaj: string;
}

export interface Departman {
  id: number;
  ad: string;
  aciklama: string | null;
  sehir: string;
  kontenjan: number;
  aktif: boolean;
  onaylananSayisi: number;
  kalanKontenjan: number;
}

export interface BelgeTuru {
  id: number;
  kod: string;
  ad: string;
  aciklama: string | null;
  zorunlu: boolean;
  maxBoyutMb: number;
  aktif: boolean;
}

export interface Belge {
  id: number;
  belgeTuruId: number;
  belgeTuruKod: string;
  belgeTuruAd: string;
  orijinalAd: string;
  icerikTuru: string | null;
  boyut: number;
  durum: BelgeDurumu;
  yoneticiNotu: string | null;
  yuklemeTarihi: string;
}

export interface DepartmanOzet {
  id: number;
  ad: string;
  sehir: string;
}

export interface Basvuru {
  id: number;
  ogrenci: {
    id: number;
    ad: string;
    soyad: string;
    tcKimlik: string;
    email: string;
    telefon: string;
    dogumTarihi: string;
  };
  okulAdi: string;
  alan: string;
  dal: string | null;
  sinif: string;
  notOrtalamasi: number | null;
  stajTuru: StajTuru;
  sehir: string;
  baslamaTarihi: string;
  bitisTarihi: string;
  ogrenciNotu: string | null;
  tercihDepartman: DepartmanOzet | null;
  atananDepartman: DepartmanOzet | null;
  durum: BasvuruDurumu;
  durumEtiketi: string;
  degerlendirmeNotu: string | null;
  basvuruTarihi: string | null;
  olusturmaTarihi: string;
  guncellemeTarihi: string;
  belgeler: Belge[];
  yapilabilecekGecisler: BasvuruDurumu[];
  duzenlenebilir: boolean;
}

export interface BasvuruOzet {
  id: number;
  ogrenciAdSoyad: string;
  tcKimlik: string;
  okulAdi: string;
  alan: string;
  sinif: string;
  sehir: string;
  stajTuru: StajTuru;
  tercihDepartmanAdi: string | null;
  atananDepartmanAdi: string | null;
  durum: BasvuruDurumu;
  durumEtiketi: string;
  baslamaTarihi: string;
  bitisTarihi: string;
  basvuruTarihi: string | null;
  guncellemeTarihi: string;
}

export interface BasvuruIstek {
  okulAdi: string;
  alan: string;
  dal: string | null;
  sinif: string;
  notOrtalamasi: number | null;
  stajTuru: StajTuru;
  sehir: string;
  tercihDepartmanId: number;
  baslamaTarihi: string;
  bitisTarihi: string;
  ogrenciNotu: string | null;
}

export interface Gecmis {
  id: number;
  eskiDurum: BasvuruDurumu | null;
  yeniDurum: BasvuruDurumu;
  yeniDurumEtiketi: string;
  aciklama: string | null;
  islemYapan: string;
  islemTarihi: string;
}

export interface Sayfa<T> {
  icerik: T[];
  sayfa: number;
  boyut: number;
  toplamKayit: number;
  toplamSayfa: number;
}

export interface Istatistik {
  toplamBasvuru: number;
  toplamOgrenci: number;
  bekleyenIkIslemi: number;
  birimdeBekleyen: number;
  onaylanan: number;
  reddedilen: number;
  durumaGore: Record<string, number>;
  departmanlar: {
    id: number;
    ad: string;
    sehir: string;
    kontenjan: number;
    onaylanan: number;
    degerlendirmede: number;
    dolulukYuzdesi: number;
  }[];
}

export interface ApiHata {
  zaman: string;
  durum: number;
  hata: string;
  mesaj: string;
  yol?: string;
  alanHatalari?: Record<string, string>;
}
