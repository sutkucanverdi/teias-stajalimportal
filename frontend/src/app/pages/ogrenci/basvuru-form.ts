import { KeyValuePipe } from '@angular/common';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { alanHatalari, BildirimService, hataMesaji } from '../../core/bildirim.service';
import { BasvuruIstek, Departman, StajTuru } from '../../core/models';
import { bugunIso } from '../../core/yardimcilar';

function tarihAraligi(grup: AbstractControl): ValidationErrors | null {
  const bas = grup.get('baslamaTarihi')?.value;
  const bit = grup.get('bitisTarihi')?.value;
  return bas && bit && bit <= bas ? { tarihAraligi: true } : null;
}

@Component({
  selector: 'app-basvuru-form',
  imports: [ReactiveFormsModule, RouterLink, KeyValuePipe],
  templateUrl: './basvuru-form.html',
})
export class BasvuruForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly bildirim = inject(BildirimService);

  /** Rota parametresi: düzenleme modunda başvuru id'si */
  readonly id = input<string>();
  protected readonly duzenleme = computed(() => !!this.id());

  protected readonly departmanlar = signal<Departman[]>([]);
  protected readonly yukleniyor = signal(true);
  protected readonly gonderiliyor = signal(false);
  protected readonly hata = signal<string | null>(null);
  protected readonly sunucuHatalari = signal<Record<string, string>>({});
  protected readonly bugun = bugunIso(1);
  protected readonly sehirler = computed(() => [...new Set(this.departmanlar().map((d) => d.sehir))].sort((a, b) => a.localeCompare(b, 'tr')));

  protected readonly form = this.fb.group(
    {
      okulAdi: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(200)]),
      alan: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(120)]),
      dal: this.fb.control<string | null>(null, Validators.maxLength(120)),
      sinif: this.fb.nonNullable.control('11', Validators.required),
      notOrtalamasi: this.fb.control<number | null>(null, [Validators.min(0), Validators.max(100)]),
      stajTuru: this.fb.nonNullable.control<StajTuru>('ZORUNLU', Validators.required),
      sehir: this.fb.nonNullable.control('', Validators.required),
      tercihDepartmanId: this.fb.control<number | null>(null, Validators.required),
      baslamaTarihi: this.fb.nonNullable.control('', Validators.required),
      bitisTarihi: this.fb.nonNullable.control('', Validators.required),
      ogrenciNotu: this.fb.control<string | null>(null, Validators.maxLength(1000)),
    },
    { validators: tarihAraligi },
  );

  /** Seçilen şehre göre filtrelenmiş birimler */
  protected readonly filtreliDepartmanlar = signal<Departman[]>([]);

  ngOnInit(): void {
    this.api.departmanlar().subscribe({
      next: (d) => {
        this.departmanlar.set(d);
        this.filtrele();
        if (!this.id()) this.yukleniyor.set(false);
      },
      error: (e) => this.bildirim.apiHatasi(e, 'Birimler yüklenemedi.'),
    });

    this.form.controls.sehir.valueChanges.subscribe(() => {
      this.filtrele();
      const secili = this.form.controls.tercihDepartmanId.value;
      if (secili && !this.filtreliDepartmanlar().some((d) => d.id === secili)) {
        this.form.controls.tercihDepartmanId.setValue(null);
      }
    });

    const id = this.id();
    if (id) {
      this.api.basvuru(+id).subscribe({
        next: (b) => {
          if (!b.duzenlenebilir) {
            this.bildirim.uyari('Bu başvuru artık düzenlenemez.');
            this.router.navigate(['/basvuru', b.id]);
            return;
          }
          this.form.patchValue({
            okulAdi: b.okulAdi,
            alan: b.alan,
            dal: b.dal,
            sinif: b.sinif,
            notOrtalamasi: b.notOrtalamasi,
            stajTuru: b.stajTuru,
            sehir: b.sehir,
            tercihDepartmanId: b.tercihDepartman?.id ?? null,
            baslamaTarihi: b.baslamaTarihi,
            bitisTarihi: b.bitisTarihi,
            ogrenciNotu: b.ogrenciNotu,
          });
          this.yukleniyor.set(false);
        },
        error: (e) => {
          this.bildirim.apiHatasi(e);
          this.router.navigate(['/ogrenci/panel']);
        },
      });
    }
  }

  protected secilenDepartman(): Departman | undefined {
    return this.departmanlar().find((d) => d.id === this.form.controls.tercihDepartmanId.value);
  }

  protected hatali(alan: keyof typeof this.form.controls): boolean {
    const c = this.form.controls[alan];
    return c.invalid && c.touched;
  }

  kaydet(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.hata.set('Lütfen işaretli alanları kontrol ediniz.');
      return;
    }
    this.gonderiliyor.set(true);
    this.hata.set(null);
    const v = this.form.getRawValue();
    const istek: BasvuruIstek = {
      ...v,
      okulAdi: v.okulAdi.trim(),
      alan: v.alan.trim(),
      dal: v.dal?.trim() || null,
      ogrenciNotu: v.ogrenciNotu?.trim() || null,
      tercihDepartmanId: v.tercihDepartmanId!,
    };
    const id = this.id();
    const islem = id ? this.api.basvuruGuncelle(+id, istek) : this.api.basvuruOlustur(istek);
    islem.subscribe({
      next: (b) => {
        this.bildirim.basari(id ? 'Başvuru bilgileriniz güncellendi.' : 'Başvuru taslağınız oluşturuldu. Şimdi belgelerinizi yükleyiniz.');
        this.router.navigate(['/basvuru', b.id]);
      },
      error: (e) => {
        this.gonderiliyor.set(false);
        this.hata.set(hataMesaji(e));
        this.sunucuHatalari.set(alanHatalari(e));
      },
    });
  }

  private filtrele(): void {
    const sehir = this.form.controls.sehir.value;
    this.filtreliDepartmanlar.set(sehir ? this.departmanlar().filter((d) => d.sehir === sehir) : this.departmanlar());
  }
}
