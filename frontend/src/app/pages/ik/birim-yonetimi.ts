import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { BildirimService } from '../../core/bildirim.service';
import { Departman } from '../../core/models';

@Component({
  selector: 'app-birim-yonetimi',
  imports: [ReactiveFormsModule],
  template: `
    <div class="sayfa-baslik">
      <div>
        <h1>Birim ve Kontenjan Yönetimi</h1>
        <p>Staj başvurusu kabul eden birimleri tanımlayın, kontenjanları güncelleyin veya başvuruya kapatın.</p>
      </div>
      <div class="eylemler">
        <button type="button" class="btn btn-birincil" (click)="yeni()">+ Yeni Birim</button>
      </div>
    </div>

    @if (formAcik()) {
      <form class="kart kart-vurgulu mb-4" [formGroup]="form" (ngSubmit)="kaydet()" novalidate>
        <div class="kart-ust"><h3>{{ duzenlenen() ? 'Birimi Düzenle' : 'Yeni Birim' }}</h3></div>
        <div class="kart-govde form-izgara">
          <div class="alan tam">
            <label for="ad" class="zorunlu">Birim Adı</label>
            <input id="ad" formControlName="ad" placeholder="Örn: Bilgi Teknolojileri Dairesi Başkanlığı" />
          </div>
          <div class="alan">
            <label for="sehir" class="zorunlu">Şehir</label>
            <input id="sehir" formControlName="sehir" placeholder="Örn: Ankara" />
          </div>
          <div class="alan">
            <label for="kontenjan" class="zorunlu">Stajyer Kontenjanı</label>
            <input id="kontenjan" type="number" min="0" formControlName="kontenjan" />
          </div>
          <div class="alan tam">
            <label for="aciklama">Açıklama / Stajyerden Beklentiler</label>
            <textarea id="aciklama" formControlName="aciklama"></textarea>
          </div>
          <div class="alan tam">
            <label class="onay-kutusu"><input type="checkbox" formControlName="aktif" /> <span>Başvuruya açık (aktif)</span></label>
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
            <tr><th>Birim</th><th>Şehir</th><th>Kontenjan</th><th>Onaylanan</th><th>Durum</th><th class="sag">İşlemler</th></tr>
          </thead>
          <tbody>
            @for (d of liste(); track d.id) {
              <tr>
                <td><strong>{{ d.ad }}</strong><div class="ikincil">{{ d.aciklama }}</div></td>
                <td>{{ d.sehir }}</td>
                <td>{{ d.kontenjan }}</td>
                <td>{{ d.onaylananSayisi }}</td>
                <td>
                  <span class="rozet" [class.rozet-yesil]="d.aktif" [class.rozet-gri]="!d.aktif">{{ d.aktif ? 'Aktif' : 'Pasif' }}</span>
                </td>
                <td class="sag">
                  <div class="esnek" style="justify-content: flex-end">
                    <button type="button" class="btn btn-ikincil btn-kucuk" (click)="duzenle(d)">Düzenle</button>
                    <button type="button" class="btn btn-tehlike btn-kucuk" (click)="sil(d)">Sil</button>
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
export class BirimYonetimi implements OnInit {
  private readonly api = inject(ApiService);
  private readonly bildirim = inject(BildirimService);
  private readonly fb = inject(FormBuilder);

  protected readonly liste = signal<Departman[]>([]);
  protected readonly formAcik = signal(false);
  protected readonly duzenlenen = signal<Departman | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    ad: ['', [Validators.required, Validators.maxLength(150)]],
    sehir: ['', Validators.required],
    kontenjan: [5, [Validators.required, Validators.min(0)]],
    aciklama: [''],
    aktif: [true],
  });

  ngOnInit(): void {
    this.yukle();
  }

  yeni(): void {
    this.duzenlenen.set(null);
    this.form.reset({ ad: '', sehir: '', kontenjan: 5, aciklama: '', aktif: true });
    this.formAcik.set(true);
  }

  duzenle(d: Departman): void {
    this.duzenlenen.set(d);
    this.form.reset({ ad: d.ad, sehir: d.sehir, kontenjan: d.kontenjan, aciklama: d.aciklama ?? '', aktif: d.aktif });
    this.formAcik.set(true);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  kaydet(): void {
    this.api.departmanKaydet(this.form.getRawValue(), this.duzenlenen()?.id).subscribe({
      next: () => {
        this.bildirim.basari('Birim kaydedildi.');
        this.formAcik.set(false);
        this.yukle();
      },
      error: (e) => this.bildirim.apiHatasi(e),
    });
  }

  sil(d: Departman): void {
    if (!confirm(`"${d.ad}" birimi silinecek. Emin misiniz?`)) return;
    this.api.departmanSil(d.id).subscribe({
      next: (c) => {
        this.bildirim.basari(c.mesaj);
        this.yukle();
      },
      error: (e) => this.bildirim.apiHatasi(e),
    });
  }

  private yukle(): void {
    this.api.departmanlar(true).subscribe({
      next: (l) => this.liste.set(l),
      error: (e) => this.bildirim.apiHatasi(e),
    });
  }
}
