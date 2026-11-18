import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { BildirimService, hataMesaji } from '../../core/bildirim.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly bildirim = inject(BildirimService);

  protected readonly gonderiliyor = signal(false);
  protected readonly hata = signal<string | null>(null);
  protected readonly aktivasyonGerekli = signal(false);
  protected readonly sifreGoster = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    kullaniciAdi: ['', Validators.required],
    sifre: ['', Validators.required],
  });

  protected readonly aktivasyonForm = this.fb.nonNullable.group({
    kod: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
  });

  onLogin(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.gonderiliyor.set(true);
    this.hata.set(null);
    const { kullaniciAdi, sifre } = this.form.getRawValue();
    this.auth.girisYap(kullaniciAdi.trim(), sifre).subscribe({
      next: (cevap) => {
        this.bildirim.basari(cevap.mesaj);
        this.router.navigateByUrl(this.auth.anaSayfa(cevap.kullanici.rol));
      },
      error: (err: HttpErrorResponse) => {
        this.gonderiliyor.set(false);
        const mesaj = hataMesaji(err, 'Giriş yapılamadı.');
        this.aktivasyonGerekli.set(err.status === 403 && mesaj.includes('aktive'));
        this.hata.set(mesaj);
      },
    });
  }

  aktiveEt(): void {
    if (this.aktivasyonForm.invalid) {
      this.aktivasyonForm.markAllAsTouched();
      return;
    }
    this.auth.aktiveEt(this.form.getRawValue().kullaniciAdi.trim(), this.aktivasyonForm.getRawValue().kod).subscribe({
      next: (c) => {
        this.bildirim.basari(c.mesaj);
        this.aktivasyonGerekli.set(false);
        this.onLogin();
      },
      error: (e) => this.bildirim.apiHatasi(e),
    });
  }
}
