import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_URL } from './api.config';
import { Kullanici, LoginCevap, Mesaj, Rol } from './models';

const TOKEN_ANAHTARI = 'teias_token';
const KULLANICI_ANAHTARI = 'teias_kullanici';
const SURE_ANAHTARI = 'teias_token_bitis';

export interface KayitIstek {
  tcKimlik: string;
  ad: string;
  soyad: string;
  email: string;
  telefon: string;
  dogumTarihi: string;
  sifre: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly _kullanici = signal<Kullanici | null>(this.kayitliKullanici());
  readonly kullanici = this._kullanici.asReadonly();
  readonly girisYapildi = computed(() => this._kullanici() !== null);
  readonly rol = computed<Rol | null>(() => this._kullanici()?.rol ?? null);

  get token(): string | null {
    const bitis = Number(localStorage.getItem(SURE_ANAHTARI) ?? 0);
    if (bitis && Date.now() > bitis) {
      this.temizle();
      return null;
    }
    return localStorage.getItem(TOKEN_ANAHTARI);
  }

  girisYap(kullaniciAdi: string, sifre: string): Observable<LoginCevap> {
    return this.http.post<LoginCevap>(`${API_URL}/auth/login`, { kullaniciAdi, sifre }).pipe(
      tap((cevap) => {
        localStorage.setItem(TOKEN_ANAHTARI, cevap.token);
        localStorage.setItem(SURE_ANAHTARI, String(Date.now() + cevap.gecerlilikSaniye * 1000));
        this.kullaniciGuncelle(cevap.kullanici);
      }),
    );
  }

  kayitOl(istek: KayitIstek): Observable<Mesaj> {
    return this.http.post<Mesaj>(`${API_URL}/auth/register`, istek);
  }

  aktiveEt(kullaniciAdi: string, kod: string): Observable<Mesaj> {
    return this.http.post<Mesaj>(`${API_URL}/auth/aktivasyon`, { kullaniciAdi, kod });
  }

  profiliYenile(): Observable<Kullanici> {
    return this.http.get<Kullanici>(`${API_URL}/auth/me`).pipe(tap((k) => this.kullaniciGuncelle(k)));
  }

  kullaniciGuncelle(k: Kullanici): void {
    localStorage.setItem(KULLANICI_ANAHTARI, JSON.stringify(k));
    this._kullanici.set(k);
  }

  cikisYap(yonlendir = true): void {
    this.temizle();
    if (yonlendir) {
      this.router.navigate(['/giris']);
    }
  }

  /** Rolüne göre kullanıcının ana sayfası */
  anaSayfa(rol: Rol | null = this.rol()): string {
    switch (rol) {
      case 'ROLE_HR':
        return '/ik/panel';
      case 'ROLE_BIRIM':
        return '/birim/basvurular';
      case 'ROLE_STUDENT':
        return '/ogrenci/panel';
      default:
        return '/giris';
    }
  }

  private temizle(): void {
    localStorage.removeItem(TOKEN_ANAHTARI);
    localStorage.removeItem(KULLANICI_ANAHTARI);
    localStorage.removeItem(SURE_ANAHTARI);
    this._kullanici.set(null);
  }

  private kayitliKullanici(): Kullanici | null {
    try {
      const ham = localStorage.getItem(KULLANICI_ANAHTARI);
      const bitis = Number(localStorage.getItem(SURE_ANAHTARI) ?? 0);
      if (!ham || !localStorage.getItem(TOKEN_ANAHTARI) || (bitis && Date.now() > bitis)) {
        return null;
      }
      return JSON.parse(ham) as Kullanici;
    } catch {
      return null;
    }
  }
}
