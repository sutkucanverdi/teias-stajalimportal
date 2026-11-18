import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { BildirimService } from '../../core/bildirim.service';
import { Departman } from '../../core/models';

/** Öğrencilerin staj yapılabilecek birimleri ve kalan kontenjanları görmesi için */
@Component({
  selector: 'app-birimler-listesi',
  imports: [FormsModule],
  template: `
    <div class="sayfa-baslik">
      <div>
        <h1>Birimler ve Kontenjanlar</h1>
        <p>Staj başvurusu kabul eden TEİAŞ birimleri. Kontenjanlar onaylanan başvurulara göre anlık güncellenir.</p>
      </div>
      <div class="eylemler">
        <select class="girdi" [(ngModel)]="sehir" aria-label="Şehir filtresi">
          <option value="">Tüm şehirler</option>
          @for (s of sehirler(); track s) {
            <option [value]="s">{{ s }}</option>
          }
        </select>
      </div>
    </div>

    @if (yukleniyor()) {
      <div class="yukleniyor">Birimler yükleniyor...</div>
    } @else {
      <div class="izgara izgara-3">
        @for (d of gosterilen(); track d.id) {
          <div class="kart">
            <div class="kart-govde">
              <div class="esnek-arasi">
                <span class="rozet rozet-lacivert">{{ d.sehir }}</span>
                @if (d.kalanKontenjan === 0) {
                  <span class="rozet rozet-kirmizi">Kontenjan dolu</span>
                } @else {
                  <span class="rozet rozet-yesil">{{ d.kalanKontenjan }} kişilik yer</span>
                }
              </div>
              <h3 class="mt-2 birim-adi">{{ d.ad }}</h3>
              <p class="metin-ikincil metin-kucuk">{{ d.aciklama }}</p>
              <div class="ilerleme"><span [style.width.%]="d.kontenjan ? (d.onaylananSayisi * 100) / d.kontenjan : 0"></span></div>
              <div class="metin-kucuk metin-ikincil mt-2">{{ d.onaylananSayisi }} / {{ d.kontenjan }} kontenjan dolu</div>
            </div>
          </div>
        }
      </div>
    }
  `,
  styles: `.birim-adi { font-size: 15px; }`,
})
export class BirimlerListesi implements OnInit {
  private readonly api = inject(ApiService);
  private readonly bildirim = inject(BildirimService);

  protected readonly yukleniyor = signal(true);
  protected readonly departmanlar = signal<Departman[]>([]);
  protected readonly sehirSinyali = signal('');
  protected readonly sehirler = computed(() => [...new Set(this.departmanlar().map((d) => d.sehir))].sort((a, b) => a.localeCompare(b, 'tr')));
  protected readonly gosterilen = computed(() =>
    this.sehirSinyali() ? this.departmanlar().filter((d) => d.sehir === this.sehirSinyali()) : this.departmanlar(),
  );

  get sehir(): string {
    return this.sehirSinyali();
  }
  set sehir(v: string) {
    this.sehirSinyali.set(v);
  }

  ngOnInit(): void {
    this.api.departmanlar().subscribe({
      next: (d) => {
        this.departmanlar.set(d);
        this.yukleniyor.set(false);
      },
      error: (e) => this.bildirim.apiHatasi(e),
    });
  }
}
