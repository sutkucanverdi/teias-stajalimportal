import { DatePipe } from '@angular/common';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { BildirimService } from '../../core/bildirim.service';
import {
  Basvuru,
  BasvuruDurumu,
  Belge,
  BelgeTuru,
  Departman,
  DURUM_ETIKETLERI,
  GECIS_ETIKETLERI,
  Gecmis,
  STAJ_TURU_ETIKETLERI,
} from '../../core/models';
import { blobAc, blobIndir, dosyaBoyutu } from '../../core/yardimcilar';
import { BasvuruAdimlari } from '../../shared/basvuru-adimlari';
import { BelgeRozet, DurumRozet } from '../../shared/durum-rozet';

interface BelgeSatiri {
  tur: BelgeTuru;
  belge: Belge | null;
}

interface DiyalogDurumu {
  tur: 'durum' | 'belgeRet';
  /** tur = durum: hedef başvuru durumu */
  hedef: BasvuruDurumu;
  /** tur = belgeRet: reddedilecek belge */
  belge: Belge | null;
  aciklama: string;
  departmanId: number | null;
  aciklamaZorunlu: boolean;
}

type Diyalog = DiyalogDurumu | null;

@Component({
  selector: 'app-basvuru-detay',
  imports: [DatePipe, FormsModule, RouterLink, DurumRozet, BelgeRozet, BasvuruAdimlari],
  templateUrl: './basvuru-detay.html',
  styleUrl: './basvuru-detay.css',
})
export class BasvuruDetay implements OnInit {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly bildirim = inject(BildirimService);
  protected readonly auth = inject(AuthService);

  readonly id = input.required<string>();

  protected readonly stajTurleri = STAJ_TURU_ETIKETLERI;
  protected readonly gecisEtiketleri = GECIS_ETIKETLERI;
  protected readonly durumEtiketleri = DURUM_ETIKETLERI;
  protected readonly boyut = dosyaBoyutu;

  protected readonly yukleniyor = signal(true);
  protected readonly basvuru = signal<Basvuru | null>(null);
  protected readonly gecmis = signal<Gecmis[]>([]);
  protected readonly belgeTurleri = signal<BelgeTuru[]>([]);
  protected readonly departmanlar = signal<Departman[]>([]);
  protected readonly yuklenenTur = signal<number | null>(null);
  protected readonly islemde = signal(false);
  protected readonly diyalog = signal<Diyalog>(null);

  protected readonly ogrenci = computed(() => this.auth.rol() === 'ROLE_STUDENT');
  protected readonly ik = computed(() => this.auth.rol() === 'ROLE_HR');
  protected readonly geriYolu = computed(() =>
    this.ogrenci() ? '/ogrenci/panel' : this.ik() ? '/ik/basvurular' : '/birim/basvurular',
  );

  /** Her aktif belge türü için yüklenmiş belge (varsa) + türü pasifleşmiş ama yüklenmiş belgeler */
  protected readonly belgeSatirlari = computed<BelgeSatiri[]>(() => {
    const b = this.basvuru();
    if (!b) return [];
    const satirlar: BelgeSatiri[] = this.belgeTurleri().map((tur) => ({
      tur,
      belge: b.belgeler.find((x) => x.belgeTuruId === tur.id) ?? null,
    }));
    for (const belge of b.belgeler) {
      if (!satirlar.some((s) => s.tur.id === belge.belgeTuruId)) {
        satirlar.push({
          tur: { id: belge.belgeTuruId, kod: belge.belgeTuruKod, ad: belge.belgeTuruAd, aciklama: null, zorunlu: false, maxBoyutMb: 5, aktif: false },
          belge,
        });
      }
    }
    return satirlar;
  });

  protected readonly eksikZorunlu = computed(() =>
    this.belgeSatirlari().filter((s) => s.tur.zorunlu && (!s.belge || s.belge.durum === 'REDDEDILDI')),
  );

  protected readonly belgeDegerlendirilebilir = computed(() => {
    const d = this.basvuru()?.durum;
    return this.ik() && !!d && d !== 'TASLAK' && d !== 'ONAYLANDI' && d !== 'REDDEDILDI';
  });

  ngOnInit(): void {
    this.yukle();
    if (!this.ogrenci()) {
      this.api.departmanlar().subscribe((d) => this.departmanlar.set(d));
    }
  }

  yukle(): void {
    const id = +this.id();
    forkJoin({
      basvuru: this.api.basvuru(id),
      gecmis: this.api.basvuruGecmisi(id),
      turler: this.api.belgeTurleri(),
    }).subscribe({
      next: ({ basvuru, gecmis, turler }) => {
        this.basvuru.set(basvuru);
        this.gecmis.set([...gecmis].reverse());
        this.belgeTurleri.set(turler);
        this.yukleniyor.set(false);
      },
      error: (e) => {
        this.bildirim.apiHatasi(e, 'Başvuru görüntülenemedi.');
        this.router.navigateByUrl(this.geriYolu());
      },
    });
  }

  // ---------------- Belgeler ----------------

  dosyaSecildi(tur: BelgeTuru, olay: Event): void {
    const girdi = olay.target as HTMLInputElement;
    const dosya = girdi.files?.[0];
    girdi.value = '';
    const b = this.basvuru();
    if (!dosya || !b) return;
    if (dosya.size > tur.maxBoyutMb * 1024 * 1024) {
      this.bildirim.hata(`Dosya en fazla ${tur.maxBoyutMb} MB olabilir.`);
      return;
    }
    this.yuklenenTur.set(tur.id);
    this.api.belgeYukle(b.id, tur.id, dosya).subscribe({
      next: () => {
        this.yuklenenTur.set(null);
        this.bildirim.basari(`${tur.ad} yüklendi.`);
        this.yukle();
      },
      error: (e) => {
        this.yuklenenTur.set(null);
        this.bildirim.apiHatasi(e, 'Belge yüklenemedi.');
      },
    });
  }

  belgeGoruntule(belge: Belge): void {
    this.api.belgeIndir(belge.id).subscribe({
      next: (blob) => blobAc(blob),
      error: (e) => this.bildirim.apiHatasi(e, 'Belge açılamadı.'),
    });
  }

  belgeIndir(belge: Belge): void {
    this.api.belgeIndir(belge.id).subscribe({
      next: (blob) => blobIndir(blob, belge.orijinalAd),
      error: (e) => this.bildirim.apiHatasi(e, 'Belge indirilemedi.'),
    });
  }

  belgeSil(belge: Belge): void {
    const b = this.basvuru();
    if (!b || !confirm(`"${belge.orijinalAd}" belgesini silmek istediğinize emin misiniz?`)) return;
    this.api.belgeSil(b.id, belge.id).subscribe({
      next: (c) => {
        this.bildirim.basari(c.mesaj);
        this.yukle();
      },
      error: (e) => this.bildirim.apiHatasi(e),
    });
  }

  belgeOnayla(belge: Belge): void {
    this.api.belgeDurumGuncelle(belge.id, 'ONAYLANDI').subscribe({
      next: () => {
        this.bildirim.basari(`${belge.belgeTuruAd} onaylandı.`);
        this.yukle();
      },
      error: (e) => this.bildirim.apiHatasi(e),
    });
  }

  belgeReddetAc(belge: Belge): void {
    this.diyalog.set({ tur: 'belgeRet', hedef: 'EKSIK_BELGE', belge, aciklama: '', departmanId: null, aciklamaZorunlu: true });
  }

  // ---------------- Başvuru işlemleri ----------------

  gonder(): void {
    const b = this.basvuru();
    if (!b) return;
    if (!confirm('Başvurunuz İnsan Kaynakları\'na gönderilecek. Gönderdikten sonra bilgilerinizi değiştiremezsiniz. Onaylıyor musunuz?')) return;
    this.islemde.set(true);
    this.api.basvuruGonder(b.id).subscribe({
      next: () => {
        this.islemde.set(false);
        this.bildirim.basari('Başvurunuz başarıyla gönderildi.');
        this.yukle();
      },
      error: (e) => {
        this.islemde.set(false);
        this.bildirim.apiHatasi(e);
      },
    });
  }

  sil(): void {
    const b = this.basvuru();
    if (!b || !confirm(`${b.id} numaralı başvuru ve yüklenen tüm belgeler kalıcı olarak silinecek. Emin misiniz?`)) return;
    this.api.basvuruSil(b.id).subscribe({
      next: (c) => {
        this.bildirim.basari(c.mesaj);
        this.router.navigateByUrl(this.geriYolu());
      },
      error: (e) => this.bildirim.apiHatasi(e),
    });
  }

  durumDiyaloguAc(hedef: BasvuruDurumu): void {
    const b = this.basvuru()!;
    const zorunlu =
      hedef === 'EKSIK_BELGE' ||
      hedef === 'REDDEDILDI' ||
      (hedef === 'IK_INCELEMESINDE' && (b.durum === 'BIRIM_DEGERLENDIRMESINDE' || b.durum === 'ONAYLANDI' || b.durum === 'REDDEDILDI'));
    this.diyalog.set({
      tur: 'durum',
      hedef,
      belge: null,
      aciklama: '',
      departmanId: b.atananDepartman?.id ?? b.tercihDepartman?.id ?? null,
      aciklamaZorunlu: zorunlu,
    });
  }

  diyaloguOnayla(): void {
    const d = this.diyalog();
    const b = this.basvuru();
    if (!d || !b) return;
    const aciklama = d.aciklama.trim();

    if (d.tur === 'belgeRet' && d.belge) {
      const belge = d.belge;
      if (!aciklama) {
        this.bildirim.uyari('Öğrenciye iletilecek açıklamayı yazınız.');
        return;
      }
      this.islemde.set(true);
      this.api.belgeDurumGuncelle(belge.id, 'REDDEDILDI', aciklama).subscribe({
        next: () => this.islemBitti(`${belge.belgeTuruAd} reddedildi.`),
        error: (e) => this.islemHatasi(e),
      });
      return;
    }

    if (d.aciklamaZorunlu && !aciklama) {
      this.bildirim.uyari('Bu işlem için açıklama yazmanız zorunludur.');
      return;
    }
    this.islemde.set(true);
    const departman = d.hedef === 'BIRIM_DEGERLENDIRMESINDE' ? d.departmanId : null;
    this.api.durumGuncelle(b.id, d.hedef, aciklama || undefined, departman).subscribe({
      next: (g) => this.islemBitti(`Başvuru durumu "${g.durumEtiketi}" olarak güncellendi.`),
      error: (e) => this.islemHatasi(e),
    });
  }

  diyaloguGuncelle(alan: 'aciklama' | 'departmanId', deger: string | number | null): void {
    const d = this.diyalog();
    if (d) this.diyalog.set({ ...d, [alan]: deger });
  }

  protected gecisEtiketi(g: BasvuruDurumu): string {
    const mevcut = this.basvuru()?.durum;
    if (g === 'IK_INCELEMESINDE') {
      if (mevcut === 'BIRIM_DEGERLENDIRMESINDE') return this.ik() ? 'Birimden Geri Al' : 'İK\'ya İade Et';
      if (mevcut === 'ONAYLANDI' || mevcut === 'REDDEDILDI') return 'Yeniden Değerlendir';
      return 'İncelemeye Al';
    }
    return GECIS_ETIKETLERI[g];
  }

  protected gecisSinifi(g: BasvuruDurumu): string {
    if (g === 'ONAYLANDI') return 'btn btn-basari btn-blok';
    if (g === 'REDDEDILDI') return 'btn btn-tehlike btn-blok';
    if (g === 'BIRIM_DEGERLENDIRMESINDE') return 'btn btn-birincil btn-blok';
    return 'btn btn-ikincil btn-blok';
  }

  private islemBitti(mesaj: string): void {
    this.islemde.set(false);
    this.diyalog.set(null);
    this.bildirim.basari(mesaj);
    this.yukle();
  }

  private islemHatasi(e: unknown): void {
    this.islemde.set(false);
    this.bildirim.apiHatasi(e);
  }
}
