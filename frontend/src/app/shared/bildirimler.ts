import { Component, inject } from '@angular/core';
import { BildirimService } from '../core/bildirim.service';

@Component({
  selector: 'app-bildirimler',
  template: `
    <div class="bildirim-alani" aria-live="polite">
      @for (b of servis.bildirimler(); track b.id) {
        <div class="bildirim" [class]="'bildirim bildirim-' + b.tur" role="status">
          <span>{{ b.mesaj }}</span>
          <button type="button" (click)="servis.kapat(b.id)" aria-label="Kapat">×</button>
        </div>
      }
    </div>
  `,
  styles: `
    .bildirim-alani {
      position: fixed;
      top: 16px;
      right: 16px;
      z-index: 200;
      display: flex;
      flex-direction: column;
      gap: 8px;
      max-width: min(420px, calc(100vw - 32px));
    }
    .bildirim {
      display: flex;
      gap: 12px;
      align-items: flex-start;
      justify-content: space-between;
      padding: 12px 14px;
      border-radius: 4px;
      border-left: 4px solid;
      background: #fff;
      box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
      font-size: 13px;
      animation: gir 0.18s ease-out;
    }
    .bildirim button {
      border: none;
      background: none;
      font-size: 18px;
      line-height: 1;
      cursor: pointer;
      color: #6b7280;
    }
    .bildirim-basari { border-color: var(--basari); }
    .bildirim-hata { border-color: var(--hata); }
    .bildirim-uyari { border-color: var(--vurgu); }
    .bildirim-bilgi { border-color: var(--bilgi); }
    @keyframes gir { from { opacity: 0; transform: translateY(-6px); } }
  `,
})
export class Bildirimler {
  protected readonly servis = inject(BildirimService);
}
