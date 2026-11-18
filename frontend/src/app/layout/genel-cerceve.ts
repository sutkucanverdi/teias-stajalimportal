import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { KurumLogo } from '../shared/kurum-logo';

/** Giriş ve kayıt ekranları için sade kurumsal çerçeve */
@Component({
  selector: 'app-genel-cerceve',
  imports: [RouterOutlet, KurumLogo],
  template: `
    <header class="baslik">
      <div class="kapsayici"><app-kurum-logo /></div>
    </header>
    <main class="icerik">
      <router-outlet />
    </main>
    <footer class="alt">
      © {{ yil }} Türkiye Elektrik İletim A.Ş. Bilgi Teknolojileri Dairesi Başkanlığı. Tüm hakları saklıdır.
    </footer>
  `,
  styles: `
    :host { display: flex; flex-direction: column; min-height: 100vh; }
    .baslik { background: var(--lacivert); border-bottom: 3px solid var(--vurgu); padding: 12px 0; }
    .icerik { flex: 1; display: flex; align-items: flex-start; justify-content: center; padding: 40px 20px; }
    .alt { text-align: center; font-size: 11px; color: #8a94a3; padding: 16px; }
  `,
})
export class GenelCerceve {
  protected readonly yil = new Date().getFullYear();
}
