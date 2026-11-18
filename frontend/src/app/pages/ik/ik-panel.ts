import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { BildirimService } from '../../core/bildirim.service';
import { BasvuruDurumu, BasvuruOzet, DURUM_ETIKETLERI, Istatistik } from '../../core/models';
import { DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-ik-panel',
  imports: [RouterLink, DatePipe],
  templateUrl: './ik-panel.html',
})
export class IkPanel implements OnInit {
  private readonly api = inject(ApiService);
  private readonly bildirim = inject(BildirimService);

  protected readonly yukleniyor = signal(true);
  protected readonly ist = signal<Istatistik | null>(null);
  protected readonly bekleyenler = signal<BasvuruOzet[]>([]);

  protected readonly durumSatirlari = computed(() => {
    const i = this.ist();
    if (!i) return [];
    const enBuyuk = Math.max(1, ...Object.values(i.durumaGore));
    return (Object.keys(i.durumaGore) as BasvuruDurumu[]).map((d) => ({
      durum: d,
      etiket: DURUM_ETIKETLERI[d],
      sayi: i.durumaGore[d],
      oran: (i.durumaGore[d] * 100) / enBuyuk,
    }));
  });

  ngOnInit(): void {
    forkJoin({
      ist: this.api.istatistikler(),
      gonderilen: this.api.basvurular({ durum: 'GONDERILDI', boyut: 8 }),
    }).subscribe({
      next: ({ ist, gonderilen }) => {
        this.ist.set(ist);
        this.bekleyenler.set(gonderilen.icerik);
        this.yukleniyor.set(false);
      },
      error: (e) => {
        this.yukleniyor.set(false);
        this.bildirim.apiHatasi(e, 'İstatistikler yüklenemedi.');
      },
    });
  }
}
