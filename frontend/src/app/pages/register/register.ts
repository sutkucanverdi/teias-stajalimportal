import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { alanHatalari, BildirimService, hataMesaji } from '../../core/bildirim.service';
import { eslesmeValidator, tcKimlikValidator } from '../../core/yardimcilar';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly bildirim = inject(BildirimService);

  protected readonly gonderiliyor = signal(false);
  protected readonly hata = signal<string | null>(null);
  protected readonly sunucuAlanHatalari = signal<Record<string, string>>({});

  protected readonly form = this.fb.nonNullable.group(
    {
      tcKimlik: ['', [Validators.required, Validators.pattern(/^\d{11}$/), tcKimlikValidator]],
      ad: ['', [Validators.required, Validators.maxLength(100)]],
      soyad: ['', [Validators.required, Validators.maxLength(100)]],
      dogumTarihi: ['', Validators.required],
      telefon: ['', [Validators.required, Validators.pattern(/^0?5\d{9}$/)]],
      email: ['', [Validators.required, Validators.email]],
      sifre: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[A-Za-zÇĞİÖŞÜçğıöşü])(?=.*\d).+$/)]],
      sifreTekrar: ['', Validators.required],
      kvkk: [false, Validators.requiredTrue],
    },
    { validators: eslesmeValidator('sifre', 'sifreTekrar') },
  );

  protected hatali(alan: keyof typeof this.form.controls): boolean {
    const c = this.form.controls[alan];
    return c.invalid && c.touched;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.hata.set('Lütfen işaretli alanları kontrol ediniz.');
      return;
    }
    this.gonderiliyor.set(true);
    this.hata.set(null);
    this.sunucuAlanHatalari.set({});
    const { sifreTekrar, kvkk, ...istek } = this.form.getRawValue();
    this.auth.kayitOl({ ...istek, telefon: istek.telefon.replace(/\s/g, ''), email: istek.email.trim() }).subscribe({
      next: (cevap) => {
        this.bildirim.basari(cevap.mesaj);
        this.router.navigate(['/giris']);
      },
      error: (err) => {
        this.gonderiliyor.set(false);
        this.hata.set(hataMesaji(err, 'Kayıt işlemi sırasında bir sunucu hatası oluştu.'));
        this.sunucuAlanHatalari.set(alanHatalari(err));
      },
    });
  }
}
