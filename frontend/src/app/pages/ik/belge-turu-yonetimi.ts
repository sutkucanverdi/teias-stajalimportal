import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { BildirimService } from '../../core/bildirim.service';
import { BelgeTuru } from '../../core/models';

@Component({
  selector: 'app-belge-turu-yonetimi',
  imports: [ReactiveFormsModule],
  template: `
    <div class="sayfa-baslik">
      <div>
        <h1>Belge Türleri</h1>
        <p>Öğrencilerden istenen evrakları tanımlayın. Zorunlu belgeler yüklenmeden başvuru gönderilemez.</p>
      </div>
      <div class="eylemler">
        <button type="button" class="btn btn-birincil" (click)="yeni()">+ Yeni Belge Türü</button>
      </div>
    </div>

    @if (formAcik()) {
      <form class="kart kart-vurgulu mb-4" [formGroup]="form" (ngSubmit)="kaydet()" novalidate>
        <div class="kart-ust"><h3>{{ duzenlenen() ? 'Belge Türünü Düzenle' : 'Yeni Belge Türü' }}</h3></div>
        <div class="kart-govde form-izgara">
          <div class="alan">
            <label for="kod" class="zorunlu">Kod</label>
            <input id="kod" formControlName="kod" placeholder="Örn: SAGLIK_RAPORU" />
            <div class="alan-ipucu">Büyük harf, rakam ve alt çizgi. Sonradan değiştirilemez.</div>
          </div>
          <div class="alan">
            <label for="ad" class="zorunlu">Belge Adı</label>
            <input id="ad" formControlName="ad" placeholder="Örn: Sağlık Raporu" />
          </div>
          <div class="alan">
            <label for="maxBoyutMb" class="zorunlu">En Büyük Dosya Boyutu (MB)</label>
            <input id="maxBoyutMb" type="number" min="1" max="10" formControlName="maxBoyutMb" />
          </div>
          <div class="alan">
            <span class="etiket">Seçenekler</span>
            <label class="onay-kutusu"><input type="checkbox" formControlName="zorunlu" /> <span>Zorunlu belge</span></label>
            <label class="onay-kutusu"><input type="checkbox" formControlName="aktif" /> <span>Aktif (başvuruda istenir)</span></label>
          </div>
          <div class="alan tam">
            <label for="aciklama">Açıklama</label>
            <textarea id="aciklama" formControlName="aciklama" placeholder="Öğrencinin belgeyi nereden alacağını açıklayın."></textarea>
          </div>
        </div>
        <div class="kart-alt">
          <button type="button" class="btn btn-ikincil" (click)="formAcik.set(false)">Vazgeç</button>
          <button type="submit" class="btn btn-birincil" [disabled]="form.invalid">Kaydet</button>
        </div>
      </form>
    }

    <div class="kart">
      <div class="tablo-sarici">
        <table class="tablo">
          <thead>
            <tr><th>Kod</th><th>Belge Adı</th><th>Zorunlu</th><th>Maks. Boyut</th><th>Durum</th><th class="sag">İşlemler</th></tr>
          </thead>
          <tbody>
            @for (b of liste(); track b.id) {
              <tr>
                <td><code>{{ b.kod }}</code></td>
                <td><strong>{{ b.ad }}</strong><div class="ikincil">{{ b.aciklama }}</div></td>
                <td>{{ b.zorunlu ? 'Evet' : 'Hayır' }}</td>
                <td>{{ b.maxBoyutMb }} MB</td>
                <td><span class="rozet" [class.rozet-yesil]="b.aktif" [class.rozet-gri]="!b.aktif">{{ b.aktif ? 'Aktif' : 'Pasif' }}</span></td>
                <td class="sag">
                  <div class="esnek" style="justify-content: flex-end">
                    <button type="button" class="btn btn-ikincil btn-kucuk" (click)="duzenle(b)">Düzenle</button>
                    <button type="button" class="btn btn-tehlike btn-kucuk" (click)="sil(b)">Sil</button>
                  </div>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class BelgeTuruYonetimi implements OnInit {
  private readonly api = inject(ApiService);
  private readonly bildirim = inject(BildirimService);
  private readonly fb = inject(FormBuilder);

  protected readonly liste = signal<BelgeTuru[]>([]);
  protected readonly formAcik = signal(false);
  protected readonly duzenlenen = signal<BelgeTuru | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    kod: ['', [Validators.required, Validators.pattern(/^[A-Z0-9_]{2,40}$/)]],
    ad: ['', [Validators.required, Validators.maxLength(150)]],
    aciklama: [''],
    zorunlu: [true],
    maxBoyutMb: [5, [Validators.required, Validators.min(1), Validators.max(10)]],
    aktif: [true],
  });

  ngOnInit(): void {
    this.yukle();
  }

  yeni(): void {
    this.duzenlenen.set(null);
    this.form.reset({ kod: '', ad: '', aciklama: '', zorunlu: true, maxBoyutMb: 5, aktif: true });
    this.form.controls.kod.enable();
    this.formAcik.set(true);
  }

  duzenle(b: BelgeTuru): void {
    this.duzenlenen.set(b);
    this.form.reset({ kod: b.kod, ad: b.ad, aciklama: b.aciklama ?? '', zorunlu: b.zorunlu, maxBoyutMb: b.maxBoyutMb, aktif: b.aktif });
    this.form.controls.kod.disable();
    this.formAcik.set(true);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  kaydet(): void {
    this.api.belgeTuruKaydet(this.form.getRawValue(), this.duzenlenen()?.id).subscribe({
      next: () => {
        this.bildirim.basari('Belge türü kaydedildi.');
        this.formAcik.set(false);
        this.yukle();
      },
      error: (e) => this.bildirim.apiHatasi(e),
    });
  }

  sil(b: BelgeTuru): void {
    if (!confirm(`"${b.ad}" belge türü silinecek. Emin misiniz?`)) return;
    this.api.belgeTuruSil(b.id).subscribe({
      next: (c) => {
        this.bildirim.basari(c.mesaj);
        this.yukle();
      },
      error: (e) => this.bildirim.apiHatasi(e),
    });
  }

  private yukle(): void {
    this.api.belgeTurleri(true).subscribe({
      next: (l) => this.liste.set(l),
      error: (e) => this.bildirim.apiHatasi(e),
    });
  }
}
