import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { BildirimService } from '../../core/bildirim.service';
import { Departman, Kullanici, Rol, ROL_ETIKETLERI } from '../../core/models';

@Component({
  selector: 'app-kullanici-yonetimi',
  imports: [ReactiveFormsModule, FormsModule, DatePipe],
  template: `
    <div class="sayfa-baslik">
      <div>
        <h1>Kullanıcı Yönetimi</h1>
        <p>İK ve birim sorumlusu hesapları açın; öğrenci veya personel hesaplarını aktif/pasif yapın.</p>
      </div>
      <div class="eylemler">
        <select class="girdi" [(ngModel)]="rolFiltresi" (ngModelChange)="yukle()" aria-label="Rol filtresi">
          <option value="">Tüm roller</option>
          <option value="ROLE_STUDENT">Öğrenciler</option>
          <option value="ROLE_HR">İnsan Kaynakları</option>
          <option value="ROLE_BIRIM">Birim Sorumluları</option>
        </select>
        <button type="button" class="btn btn-birincil" (click)="formAcik.set(!formAcik())">+ Personel Hesabı</button>
      </div>
    </div>

    @if (formAcik()) {
      <form class="kart kart-vurgulu mb-4" [formGroup]="form" (ngSubmit)="olustur()" novalidate>
        <div class="kart-ust"><h3>Yeni Personel Hesabı</h3></div>
        <div class="kart-govde form-izgara">
          <div class="alan">
            <label for="ad" class="zorunlu">Ad</label>
            <input id="ad" formControlName="ad" />
          </div>
          <div class="alan">
            <label for="soyad" class="zorunlu">Soyad</label>
            <input id="soyad" formControlName="soyad" />
          </div>
          <div class="alan">
            <label for="kullaniciAdi" class="zorunlu">Kullanıcı Adı</label>
            <input id="kullaniciAdi" formControlName="kullaniciAdi" placeholder="ör. ad.soyad" autocomplete="off" />
            <div class="alan-ipucu">Küçük harf, rakam, nokta ve alt çizgi.</div>
          </div>
          <div class="alan">
            <label for="sifre" class="zorunlu">Geçici Şifre</label>
            <input id="sifre" type="password" formControlName="sifre" autocomplete="new-password" />
            <div class="alan-ipucu">En az 8 karakter. Personel ilk girişte değiştirmelidir.</div>
          </div>
          <div class="alan">
            <label for="email">E-posta</label>
            <input id="email" type="email" formControlName="email" />
          </div>
          <div class="alan">
            <label for="telefon">Telefon</label>
            <input id="telefon" formControlName="telefon" />
          </div>
          <div class="alan">
            <label for="rol" class="zorunlu">Rol</label>
            <select id="rol" formControlName="rol">
              <option value="ROLE_BIRIM">Birim Sorumlusu</option>
              <option value="ROLE_HR">İnsan Kaynakları</option>
            </select>
          </div>
          @if (form.controls.rol.value === 'ROLE_BIRIM') {
            <div class="alan">
              <label for="departmanId" class="zorunlu">Sorumlu Olduğu Birim</label>
              <select id="departmanId" formControlName="departmanId">
                <option [ngValue]="null" disabled>Birim seçiniz</option>
                @for (d of departmanlar(); track d.id) {
                  <option [ngValue]="d.id">{{ d.ad }}</option>
                }
              </select>
            </div>
          }
        </div>
        <div class="kart-alt">
          <button type="button" class="btn btn-ikincil" (click)="formAcik.set(false)">Vazgeç</button>
          <button type="submit" class="btn btn-birincil" [disabled]="form.invalid">Hesabı Oluştur</button>
        </div>
      </form>
    }

    <div class="kart">
      <div class="tablo-sarici">
        <table class="tablo">
          <thead>
            <tr><th>Ad Soyad</th><th>Kullanıcı Adı</th><th>Rol</th><th>Birim</th><th>İletişim</th><th>Kayıt</th><th>Durum</th><th></th></tr>
          </thead>
          <tbody>
            @for (k of liste(); track k.id) {
              <tr>
                <td><strong>{{ k.ad }} {{ k.soyad }}</strong></td>
                <td>{{ k.kullaniciAdi }}</td>
                <td>{{ rolEtiketleri[k.rol] }}</td>
                <td class="ikincil">{{ k.departmanAdi || '—' }}</td>
                <td class="ikincil">{{ k.email }}<br />{{ k.telefon }}</td>
                <td class="ikincil">{{ k.olusturmaTarihi | date: 'dd.MM.yyyy' }}</td>
                <td><span class="rozet" [class.rozet-yesil]="k.aktifMi" [class.rozet-gri]="!k.aktifMi">{{ k.aktifMi ? 'Aktif' : 'Pasif' }}</span></td>
                <td class="sag">
                  @if (k.id !== auth.kullanici()?.id) {
                    <button type="button" class="btn btn-kucuk" [class.btn-tehlike]="k.aktifMi" [class.btn-ikincil]="!k.aktifMi" (click)="aktiflik(k)">
                      {{ k.aktifMi ? 'Pasif Yap' : 'Aktif Yap' }}
                    </button>
                  }
                </td>
              </tr>
            } @empty {
              <tr><td colspan="8" class="bos-durum">Kullanıcı bulunamadı.</td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class KullaniciYonetimi implements OnInit {
  private readonly api = inject(ApiService);
  private readonly bildirim = inject(BildirimService);
  private readonly fb = inject(FormBuilder);
  protected readonly auth = inject(AuthService);
  protected readonly rolEtiketleri = ROL_ETIKETLERI;

  protected readonly liste = signal<Kullanici[]>([]);
  protected readonly departmanlar = signal<Departman[]>([]);
  protected readonly formAcik = signal(false);
  protected rolFiltresi: Rol | '' = '';

  protected readonly form = this.fb.group({
    ad: this.fb.nonNullable.control('', Validators.required),
    soyad: this.fb.nonNullable.control('', Validators.required),
    kullaniciAdi: this.fb.nonNullable.control('', [Validators.required, Validators.pattern(/^[a-z0-9._]{3,50}$/)]),
    sifre: this.fb.nonNullable.control('', [Validators.required, Validators.minLength(8)]),
    email: this.fb.control<string | null>(null, Validators.email),
    telefon: this.fb.control<string | null>(null),
    rol: this.fb.nonNullable.control<Rol>('ROLE_BIRIM', Validators.required),
    departmanId: this.fb.control<number | null>(null),
  });

  ngOnInit(): void {
    this.api.departmanlar(true).subscribe((d) => this.departmanlar.set(d));
    this.yukle();
  }

  yukle(): void {
    this.api.kullanicilar(this.rolFiltresi).subscribe({
      next: (l) => this.liste.set(l),
      error: (e) => this.bildirim.apiHatasi(e),
    });
  }

  olustur(): void {
    const v = this.form.getRawValue();
    this.api.personelOlustur({ ...v, email: v.email || null, telefon: v.telefon || null, departmanId: v.rol === 'ROLE_BIRIM' ? v.departmanId : null }).subscribe({
      next: (k) => {
        this.bildirim.basari(`${k.ad} ${k.soyad} için hesap oluşturuldu.`);
        this.form.reset({ rol: 'ROLE_BIRIM' });
        this.formAcik.set(false);
        this.yukle();
      },
      error: (e) => this.bildirim.apiHatasi(e),
    });
  }

  aktiflik(k: Kullanici): void {
    this.api.aktiflikDegistir(k.id, !k.aktifMi).subscribe({
      next: (g) => {
        this.bildirim.basari(`${g.ad} ${g.soyad} ${g.aktifMi ? 'aktif' : 'pasif'} yapıldı.`);
        this.yukle();
      },
      error: (e) => this.bildirim.apiHatasi(e),
    });
  }
}
