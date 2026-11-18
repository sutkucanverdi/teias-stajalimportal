import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Resmi T.C. Kimlik No algoritması (backend ile aynı kural) */
export function tcKimlikGecerliMi(tc: string | null | undefined): boolean {
  if (!tc || !/^[1-9]\d{10}$/.test(tc)) {
    return false;
  }
  const d = tc.split('').map(Number);
  const tek = d[0] + d[2] + d[4] + d[6] + d[8];
  const cift = d[1] + d[3] + d[5] + d[7];
  const onuncu = (((tek * 7 - cift) % 10) + 10) % 10;
  if (onuncu !== d[9]) {
    return false;
  }
  const toplam = d.slice(0, 10).reduce((a, b) => a + b, 0);
  return toplam % 10 === d[10];
}

export const tcKimlikValidator: ValidatorFn = (c: AbstractControl): ValidationErrors | null =>
  !c.value || tcKimlikGecerliMi(String(c.value)) ? null : { tcKimlik: true };

export function eslesmeValidator(alan: string, tekrarAlan: string): ValidatorFn {
  return (grup: AbstractControl): ValidationErrors | null => {
    const a = grup.get(alan)?.value;
    const b = grup.get(tekrarAlan)?.value;
    return a && b && a !== b ? { eslesmiyor: true } : null;
  };
}

export function dosyaBoyutu(bayt: number): string {
  if (bayt < 1024) return `${bayt} B`;
  if (bayt < 1024 * 1024) return `${(bayt / 1024).toFixed(0)} KB`;
  return `${(bayt / 1024 / 1024).toFixed(1)} MB`;
}

/** Blob'u tarayıcıda indirir */
export function blobIndir(blob: Blob, dosyaAdi: string): void {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = dosyaAdi;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

/** Blob'u yeni sekmede açar (PDF / resim önizleme) */
export function blobAc(blob: Blob): void {
  const url = URL.createObjectURL(blob);
  window.open(url, '_blank', 'noopener');
  setTimeout(() => URL.revokeObjectURL(url), 60_000);
}

export function bugunIso(gunEkle = 0): string {
  const t = new Date();
  t.setDate(t.getDate() + gunEkle);
  const ay = String(t.getMonth() + 1).padStart(2, '0');
  const gun = String(t.getDate()).padStart(2, '0');
  return `${t.getFullYear()}-${ay}-${gun}`;
}
