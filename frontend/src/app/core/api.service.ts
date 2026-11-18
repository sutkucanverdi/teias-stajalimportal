import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from './api.config';
import {
  Basvuru,
  BasvuruDurumu,
  BasvuruIstek,
  BasvuruOzet,
  Belge,
  BelgeDurumu,
  BelgeTuru,
  Departman,
  Gecmis,
  Istatistik,
  Kullanici,
  Mesaj,
  Rol,
  Sayfa,
  StajTuru,
} from './models';

export interface BasvuruFiltre {
  durum?: BasvuruDurumu | '';
  departmanId?: number | null;
  sehir?: string;
  stajTuru?: StajTuru | '';
  arama?: string;
  sayfa?: number;
  boyut?: number;
}

/** Spring Boot REST API'sinin tüm uç noktaları */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  // ---------- Başvurular ----------
  benimBasvurularim(): Observable<Basvuru[]> {
    return this.http.get<Basvuru[]>(`${API_URL}/basvurular/benim`);
  }

  basvuru(id: number): Observable<Basvuru> {
    return this.http.get<Basvuru>(`${API_URL}/basvurular/${id}`);
  }

  basvuruGecmisi(id: number): Observable<Gecmis[]> {
    return this.http.get<Gecmis[]>(`${API_URL}/basvurular/${id}/gecmis`);
  }

  basvuruOlustur(istek: BasvuruIstek): Observable<Basvuru> {
    return this.http.post<Basvuru>(`${API_URL}/basvurular`, istek);
  }

  basvuruGuncelle(id: number, istek: BasvuruIstek): Observable<Basvuru> {
    return this.http.put<Basvuru>(`${API_URL}/basvurular/${id}`, istek);
  }

  basvuruGonder(id: number): Observable<Basvuru> {
    return this.http.post<Basvuru>(`${API_URL}/basvurular/${id}/gonder`, {});
  }

  basvuruSil(id: number): Observable<Mesaj> {
    return this.http.delete<Mesaj>(`${API_URL}/basvurular/${id}`);
  }

  basvurular(f: BasvuruFiltre): Observable<Sayfa<BasvuruOzet>> {
    return this.http.get<Sayfa<BasvuruOzet>>(`${API_URL}/basvurular`, { params: this.filtreParametreleri(f) });
  }

  basvurulariDisaAktar(f: BasvuruFiltre): Observable<Blob> {
    return this.http.get(`${API_URL}/basvurular/disa-aktar`, { params: this.filtreParametreleri(f), responseType: 'blob' });
  }

  durumGuncelle(id: number, durum: BasvuruDurumu, aciklama?: string, departmanId?: number | null): Observable<Basvuru> {
    return this.http.put<Basvuru>(`${API_URL}/basvurular/${id}/durum`, { durum, aciklama, departmanId });
  }

  // ---------- Belgeler ----------
  belgeYukle(basvuruId: number, belgeTuruId: number, dosya: File): Observable<Belge> {
    const form = new FormData();
    form.append('dosya', dosya, dosya.name);
    return this.http.post<Belge>(`${API_URL}/basvurular/${basvuruId}/belgeler`, form, {
      params: { belgeTuruId },
    });
  }

  belgeSil(basvuruId: number, belgeId: number): Observable<Mesaj> {
    return this.http.delete<Mesaj>(`${API_URL}/basvurular/${basvuruId}/belgeler/${belgeId}`);
  }

  belgeIndir(belgeId: number): Observable<Blob> {
    return this.http.get(`${API_URL}/belgeler/${belgeId}/indir`, { responseType: 'blob' });
  }

  belgeDurumGuncelle(belgeId: number, durum: BelgeDurumu, not?: string): Observable<Belge> {
    return this.http.put<Belge>(`${API_URL}/belgeler/${belgeId}/durum`, { durum, not });
  }

  // ---------- Birimler ----------
  departmanlar(tumu = false): Observable<Departman[]> {
    return this.http.get<Departman[]>(`${API_URL}/departmanlar`, { params: { tumu } });
  }

  departmanKaydet(d: Partial<Departman>, id?: number): Observable<Departman> {
    return id ? this.http.put<Departman>(`${API_URL}/departmanlar/${id}`, d) : this.http.post<Departman>(`${API_URL}/departmanlar`, d);
  }

  departmanSil(id: number): Observable<Mesaj> {
    return this.http.delete<Mesaj>(`${API_URL}/departmanlar/${id}`);
  }

  // ---------- Belge türleri ----------
  belgeTurleri(tumu = false): Observable<BelgeTuru[]> {
    return this.http.get<BelgeTuru[]>(`${API_URL}/belge-turleri`, { params: { tumu } });
  }

  belgeTuruKaydet(b: Partial<BelgeTuru>, id?: number): Observable<BelgeTuru> {
    return id ? this.http.put<BelgeTuru>(`${API_URL}/belge-turleri/${id}`, b) : this.http.post<BelgeTuru>(`${API_URL}/belge-turleri`, b);
  }

  belgeTuruSil(id: number): Observable<Mesaj> {
    return this.http.delete<Mesaj>(`${API_URL}/belge-turleri/${id}`);
  }

  // ---------- Kullanıcılar ----------
  kullanicilar(rol?: Rol | ''): Observable<Kullanici[]> {
    return this.http.get<Kullanici[]>(`${API_URL}/users`, { params: rol ? { rol } : {} });
  }

  personelOlustur(istek: {
    ad: string;
    soyad: string;
    kullaniciAdi: string;
    email: string | null;
    telefon: string | null;
    sifre: string;
    rol: Rol;
    departmanId: number | null;
  }): Observable<Kullanici> {
    return this.http.post<Kullanici>(`${API_URL}/users`, istek);
  }

  aktiflikDegistir(id: number, aktif: boolean): Observable<Kullanici> {
    return this.http.put<Kullanici>(`${API_URL}/users/${id}/aktiflik`, { aktif });
  }

  // ---------- Profil ----------
  profilGuncelle(email: string, telefon: string): Observable<Kullanici> {
    return this.http.put<Kullanici>(`${API_URL}/auth/me`, { email, telefon });
  }

  sifreDegistir(mevcutSifre: string, yeniSifre: string): Observable<Mesaj> {
    return this.http.put<Mesaj>(`${API_URL}/auth/sifre`, { mevcutSifre, yeniSifre });
  }

  // ---------- İstatistik ----------
  istatistikler(): Observable<Istatistik> {
    return this.http.get<Istatistik>(`${API_URL}/istatistikler`);
  }

  private filtreParametreleri(f: BasvuruFiltre): HttpParams {
    let p = new HttpParams();
    if (f.durum) p = p.set('durum', f.durum);
    if (f.departmanId) p = p.set('departmanId', f.departmanId);
    if (f.sehir) p = p.set('sehir', f.sehir);
    if (f.stajTuru) p = p.set('stajTuru', f.stajTuru);
    if (f.arama) p = p.set('arama', f.arama);
    if (f.sayfa !== undefined) p = p.set('page', f.sayfa);
    if (f.boyut !== undefined) p = p.set('size', f.boyut);
    return p;
  }
}
