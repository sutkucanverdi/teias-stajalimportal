import { Component, input } from '@angular/core';

/** Sade kurum işareti: iletim direği + yıldırım simgesi ve kurum adı */
@Component({
  selector: 'app-kurum-logo',
  template: `
    <span class="logo" [class.acik]="acik()">
      <svg viewBox="0 0 40 40" width="34" height="34" aria-hidden="true">
        <rect width="40" height="40" rx="6" [attr.fill]="acik() ? '#ffffff' : '#002855'" />
        <path d="M20 6 L12 34 M20 6 L28 34 M14.5 25 H25.5 M16.5 17 H23.5 M10 13 H30"
              fill="none" [attr.stroke]="acik() ? '#002855' : '#ffffff'" stroke-width="2" stroke-linecap="round" />
        <path d="M22 18 L18.5 24 H21.5 L19 30 L25 22.5 H21.8 L24 18 Z" fill="#f2a900" />
      </svg>
      <span class="yazi">
        <strong>TEİAŞ</strong>
        <small>Türkiye Elektrik İletim A.Ş.</small>
      </span>
    </span>
  `,
  styles: `
    .logo { display: inline-flex; align-items: center; gap: 10px; color: #fff; }
    .logo.acik { color: var(--lacivert); }
    .yazi { display: flex; flex-direction: column; line-height: 1.1; }
    strong { font-size: 17px; letter-spacing: 1px; }
    small { font-size: 11px; opacity: 0.85; font-weight: 400; }
  `,
})
export class KurumLogo {
  /** Açık zemin üzerinde kullanım için */
  readonly acik = input(false);
}
