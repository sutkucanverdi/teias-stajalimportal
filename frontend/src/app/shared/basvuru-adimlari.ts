import { Component, computed, input } from '@angular/core';
import { BasvuruDurumu } from '../core/models';

const ADIMLAR = ['Başvuru Hazırlama', 'İK Ön İnceleme', 'Birim Değerlendirmesi', 'Sonuç'];

function aktifAdim(d: BasvuruDurumu): number {
  switch (d) {
    case 'TASLAK':
      return 0;
    case 'GONDERILDI':
    case 'IK_INCELEMESINDE':
    case 'EKSIK_BELGE':
      return 1;
    case 'BIRIM_DEGERLENDIRMESINDE':
      return 2;
    default:
      return 3;
  }
}

/** Başvurunun iş akışındaki yerini gösteren adım çubuğu */
@Component({
  selector: 'app-basvuru-adimlari',
  template: `
    <div class="adimlar" role="list">
      @for (a of adimlar(); track a.ad) {
        <div class="adim" role="listitem" [class.tamam]="a.tamam" [class.aktif]="a.aktif" [class.red]="a.red">
          {{ a.ad }}
        </div>
      }
    </div>
  `,
})
export class BasvuruAdimlari {
  readonly durum = input.required<BasvuruDurumu>();

  readonly adimlar = computed(() => {
    const d = this.durum();
    const aktif = aktifAdim(d);
    return ADIMLAR.map((ad, i) => {
      const sonAdim = i === ADIMLAR.length - 1;
      return {
        ad: sonAdim && d === 'ONAYLANDI' ? 'Onaylandı' : sonAdim && d === 'REDDEDILDI' ? 'Reddedildi' : ad,
        tamam: i < aktif || (sonAdim && d === 'ONAYLANDI'),
        aktif: i === aktif && !(sonAdim && (d === 'ONAYLANDI' || d === 'REDDEDILDI')),
        red: sonAdim && d === 'REDDEDILDI',
      };
    });
  });
}
