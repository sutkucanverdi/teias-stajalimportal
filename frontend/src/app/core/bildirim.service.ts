import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { ApiHata } from './models';

export interface Bildirim {
  id: number;
  tur: 'basari' | 'hata' | 'uyari' | 'bilgi';
  mesaj: string;
}

/** Sağ üst köşede gösterilen kısa bildirimler (alert() yerine) */
@Injectable({ providedIn: 'root' })
export class BildirimService {
  private sayac = 0;
  readonly bildirimler = signal<Bildirim[]>([]);

  basari(mesaj: string): void {
    this.ekle('basari', mesaj);
  }

  hata(mesaj: string): void {
    this.ekle('hata', mesaj, 7000);
  }

  uyari(mesaj: string): void {
    this.ekle('uyari', mesaj, 6000);
  }

  bilgi(mesaj: string): void {
    this.ekle('bilgi', mesaj);
  }

  /** Sunucu hatasından okunabilir mesaj çıkarıp gösterir */
  apiHatasi(hata: unknown, varsayilan = 'İşlem sırasında bir hata oluştu.'): void {
    this.hata(hataMesaji(hata, varsayilan));
  }

  kapat(id: number): void {
    this.bildirimler.update((l) => l.filter((b) => b.id !== id));
  }

  private ekle(tur: Bildirim['tur'], mesaj: string, sure = 4500): void {
    const id = ++this.sayac;
    this.bildirimler.update((l) => [...l, { id, tur, mesaj }]);
    setTimeout(() => this.kapat(id), sure);
  }
}

export function hataMesaji(hata: unknown, varsayilan = 'İşlem sırasında bir hata oluştu.'): string {
  if (hata instanceof HttpErrorResponse) {
    if (hata.status === 0) {
      return 'Sunucuya ulaşılamıyor. Lütfen bağlantınızı kontrol ediniz.';
    }
    const govde = hata.error as Partial<ApiHata> | null;
    if (govde && typeof govde === 'object' && govde.mesaj) {
      const alanlar = govde.alanHatalari ? Object.values(govde.alanHatalari) : [];
      return alanlar.length ? `${govde.mesaj} ${alanlar.join(' ')}` : govde.mesaj;
    }
  }
  return varsayilan;
}

export function alanHatalari(hata: unknown): Record<string, string> {
  if (hata instanceof HttpErrorResponse && hata.error && typeof hata.error === 'object') {
    return (hata.error as ApiHata).alanHatalari ?? {};
  }
  return {};
}
