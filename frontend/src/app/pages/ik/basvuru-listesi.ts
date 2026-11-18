import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subject, debounceTime } from 'rxjs';
import { ApiService, BasvuruFiltre } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { BildirimService } from '../../core/bildirim.service';
import { BasvuruDurumu, BasvuruOzet, Departman, DURUM_ETIKETLERI, Sayfa, StajTuru } from '../../core/models';
import { blobIndir } from '../../core/yardimcilar';
import { DurumRozet } from '../../shared/durum-rozet';

/** İK (tüm başvurular) ve birim sorumlusu (biriminize yönlendirilenler) için ortak liste */
@Component({
  selector: 'app-basvuru-listesi',
  imports: [FormsModule, RouterLink, DatePipe, DurumRozet],
  templateUrl: './basvuru-listesi.html',
})
export class BasvuruListesi implements OnInit {
  private readonly api = inject(ApiService);
  private readonly bildirim = inject(BildirimService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  protected readonly ik = computed(() => this.auth.rol() === 'ROLE_HR');
  protected readonly durumlar = (Object.keys(DURUM_ETIKETLERI) as BasvuruDurumu[])
    .filter((d) => d !== 'TASLAK')
    .map((d) => ({ deger: d, etiket: DURUM_ETIKETLERI[d] }));

  protected readonly yukleniyor = signal(true);
  protected readonly sonuc = signal<Sayfa<BasvuruOzet> | null>(null);
  protected readonly departmanlar = signal<Departman[]>([]);
  protected readonly sehirler = computed(() => [...new Set(this.departmanlar().map((d) => d.sehir))].sort((a, b) => a.localeCompare(b, 'tr')));

  protected filtre: {
    durum: BasvuruDurumu | '';
    departmanId: number | null;
    sehir: string;
    stajTuru: StajTuru | '';
    arama: string;
  } = { durum: '', departmanId: null, sehir: '', stajTuru: '', arama: '' };
  protected sayfa = 0;
  protected readonly boyut = 15;

  private readonly arama$ = new Subject<void>();

  ngOnInit(): void {
    const q = this.route.snapshot.queryParamMap;
    this.filtre.durum = (q.get('durum') as BasvuruDurumu) ?? '';
    if (this.ik()) {
      this.api.departmanlar(true).subscribe((d) => this.departmanlar.set(d));
    }
    this.arama$.pipe(debounceTime(350)).subscribe(() => this.ara());
    this.yukle();
  }

  aramaDegisti(): void {
    this.arama$.next();
  }

  ara(): void {
    this.sayfa = 0;
    this.router.navigate([], { queryParams: { durum: this.filtre.durum || null }, replaceUrl: true });
    this.yukle();
  }

  temizle(): void {
    this.filtre = { durum: '', departmanId: null, sehir: '', stajTuru: '', arama: '' };
    this.ara();
  }

  sayfayaGit(s: number): void {
    this.sayfa = s;
    this.yukle();
  }

  disaAktar(): void {
    this.api.basvurulariDisaAktar(this.istekFiltresi()).subscribe({
      next: (blob) => blobIndir(blob, `staj-basvurulari-${new Date().toISOString().slice(0, 10)}.csv`),
      error: (e) => this.bildirim.apiHatasi(e, 'Dışa aktarım yapılamadı.'),
    });
  }

  ac(b: BasvuruOzet): void {
    this.router.navigate(['/basvuru', b.id]);
  }

  private istekFiltresi(): BasvuruFiltre {
    return { ...this.filtre, arama: this.filtre.arama.trim() };
  }

  private yukle(): void {
    this.yukleniyor.set(true);
    this.api.basvurular({ ...this.istekFiltresi(), sayfa: this.sayfa, boyut: this.boyut }).subscribe({
      next: (s) => {
        this.sonuc.set(s);
        this.yukleniyor.set(false);
      },
      error: (e) => {
        this.yukleniyor.set(false);
        this.bildirim.apiHatasi(e, 'Başvurular yüklenemedi.');
      },
    });
  }
}
