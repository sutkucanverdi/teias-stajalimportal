import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { BildirimService } from '../../core/bildirim.service';
import { Basvuru, BelgeTuru, STAJ_TURU_ETIKETLERI } from '../../core/models';
import { BasvuruAdimlari } from '../../shared/basvuru-adimlari';
import { DurumRozet } from '../../shared/durum-rozet';

@Component({
  selector: 'app-ogrenci-panel',
  imports: [RouterLink, DatePipe, DurumRozet, BasvuruAdimlari],
  templateUrl: './ogrenci-panel.html',
})
export class OgrenciPanel implements OnInit {
  private readonly api = inject(ApiService);
  private readonly bildirim = inject(BildirimService);
  protected readonly auth = inject(AuthService);
  protected readonly stajTurleri = STAJ_TURU_ETIKETLERI;

  protected readonly yukleniyor = signal(true);
  protected readonly basvurular = signal<Basvuru[]>([]);
  protected readonly belgeTurleri = signal<BelgeTuru[]>([]);

  /** Reddedilmemiş en güncel başvuru (aynı anda yalnızca bir aktif başvuru olabilir) */
  protected readonly aktif = computed(() => this.basvurular().find((b) => b.durum !== 'REDDEDILDI') ?? null);
  protected readonly gecmis = computed(() => this.basvurular().filter((b) => b !== this.aktif()));
  protected readonly yeniBasvuruYapabilir = computed(() => this.aktif() === null);

  protected readonly eksikBelgeler = computed(() => {
    const b = this.aktif();
    if (!b) return [];
    const yuklenen = new Set(b.belgeler.filter((x) => x.durum !== 'REDDEDILDI').map((x) => x.belgeTuruId));
    return this.belgeTurleri().filter((t) => t.zorunlu && !yuklenen.has(t.id));
  });

  protected readonly eksikBelgeAdlari = computed(() => this.eksikBelgeler().map((t) => t.ad).join(', '));

  ngOnInit(): void {
    forkJoin({ basvurular: this.api.benimBasvurularim(), turler: this.api.belgeTurleri() }).subscribe({
      next: ({ basvurular, turler }) => {
        this.basvurular.set(basvurular);
        this.belgeTurleri.set(turler);
        this.yukleniyor.set(false);
      },
      error: (e) => {
        this.yukleniyor.set(false);
        this.bildirim.apiHatasi(e, 'Başvuru bilgileriniz yüklenemedi.');
      },
    });
  }
}
