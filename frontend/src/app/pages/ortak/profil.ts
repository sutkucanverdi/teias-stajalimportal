import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { BildirimService } from '../../core/bildirim.service';
import { ROL_ETIKETLERI } from '../../core/models';
import { eslesmeValidator } from '../../core/yardimcilar';

@Component({
  selector: 'app-profil',
  imports: [ReactiveFormsModule],
  template: `
    <div class="sayfa-baslik">
      <div>
        <h1>Profilim</h1>
        <p>İletişim bilgilerinizi güncelleyebilir ve şifrenizi değiştirebilirsiniz.</p>
      </div>
    </div>

    @if (auth.kullanici(); as k) {
      <div class="izgara izgara-2">
        <div class="kart">
          <div class="kart-ust"><h3>Hesap Bilgileri</h3></div>
          <div class="kart-govde">
            <dl class="bilgi-listesi">
              <dt>Ad Soyad</dt><dd>{{ k.ad }} {{ k.soyad }}</dd>
              <dt>Kullanıcı Adı</dt><dd>{{ k.kullaniciAdi }}</dd>
              <dt>Rol</dt><dd>{{ rolEtiketleri[k.rol] }}</dd>
              @if (k.departmanAdi) {
                <dt>Birim</dt><dd>{{ k.departmanAdi }}</dd>
              }
            </dl>
          </div>
          <form [formGroup]="iletisim" (ngSubmit)="iletisimKaydet()" novalidate>
            <div class="kart-govde ust-cizgi form-izgara">
              <div class="alan">
                <label for="email" class="zorunlu">E-posta</label>
                <input id="email" type="email" formControlName="email" />
              </div>
              <div class="alan">
                <label for="telefon" class="zorunlu">Telefon</label>
                <input id="telefon" formControlName="telefon" placeholder="05XXXXXXXXX" />
                @if (iletisim.controls.telefon.invalid && iletisim.controls.telefon.touched) {
                  <div class="alan-hata">05XXXXXXXXX formatında giriniz.</div>
                }
              </div>
            </div>
            <div class="kart-alt">
              <button type="submit" class="btn btn-birincil" [disabled]="iletisim.invalid || kaydediliyor()">İletişim Bilgilerini Kaydet</button>
            </div>
          </form>
        </div>

        <form class="kart" [formGroup]="sifre" (ngSubmit)="sifreDegistir()" novalidate>
          <div class="kart-ust"><h3>Şifre Değiştir</h3></div>
          <div class="kart-govde">
            <div class="alan mb-4">
              <label for="mevcut" class="zorunlu">Mevcut Şifre</label>
              <input id="mevcut" type="password" formControlName="mevcutSifre" autocomplete="current-password" />
            </div>
            <div class="alan mb-4">
              <label for="yeni" class="zorunlu">Yeni Şifre</label>
              <input id="yeni" type="password" formControlName="yeniSifre" autocomplete="new-password" />
              <div class="alan-ipucu">En az 8 karakter; harf ve rakam içermelidir.</div>
            </div>
            <div class="alan">
              <label for="tekrar" class="zorunlu">Yeni Şifre (Tekrar)</label>
              <input id="tekrar" type="password" formControlName="yeniSifreTekrar" autocomplete="new-password" />
              @if (sifre.hasError('eslesmiyor') && sifre.controls.yeniSifreTekrar.touched) {
                <div class="alan-hata">Şifreler eşleşmiyor.</div>
              }
            </div>
          </div>
          <div class="kart-alt">
            <button type="submit" class="btn btn-birincil" [disabled]="sifre.invalid || kaydediliyor()">Şifreyi Değiştir</button>
          </div>
        </form>
      </div>
    }
  `,
})
export class Profil implements OnInit {
  private readonly api = inject(ApiService);
  private readonly bildirim = inject(BildirimService);
  private readonly fb = inject(FormBuilder);
  protected readonly auth = inject(AuthService);
  protected readonly rolEtiketleri = ROL_ETIKETLERI;
  protected readonly kaydediliyor = signal(false);

  protected readonly iletisim = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    telefon: ['', [Validators.required, Validators.pattern(/^0?5\d{9}$/)]],
  });

  protected readonly sifre = this.fb.nonNullable.group(
    {
      mevcutSifre: ['', Validators.required],
      yeniSifre: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[A-Za-zÇĞİÖŞÜçğıöşü])(?=.*\d).+$/)]],
      yeniSifreTekrar: ['', Validators.required],
    },
    { validators: eslesmeValidator('yeniSifre', 'yeniSifreTekrar') },
  );

  ngOnInit(): void {
    this.auth.profiliYenile().subscribe((k) => this.iletisim.reset({ email: k.email ?? '', telefon: k.telefon ?? '' }));
  }

  iletisimKaydet(): void {
    this.kaydediliyor.set(true);
    const { email, telefon } = this.iletisim.getRawValue();
    this.api.profilGuncelle(email, telefon).subscribe({
      next: (k) => {
        this.kaydediliyor.set(false);
        this.auth.kullaniciGuncelle(k);
        this.bildirim.basari('İletişim bilgileriniz güncellendi.');
      },
      error: (e) => {
        this.kaydediliyor.set(false);
        this.bildirim.apiHatasi(e);
      },
    });
  }

  sifreDegistir(): void {
    this.kaydediliyor.set(true);
    const { mevcutSifre, yeniSifre } = this.sifre.getRawValue();
    this.api.sifreDegistir(mevcutSifre, yeniSifre).subscribe({
      next: (c) => {
        this.kaydediliyor.set(false);
        this.sifre.reset();
        this.bildirim.basari(c.mesaj);
      },
      error: (e) => {
        this.kaydediliyor.set(false);
        this.bildirim.apiHatasi(e);
      },
    });
  }
}
