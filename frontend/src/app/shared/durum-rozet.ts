import { Component, computed, input } from '@angular/core';
import { BasvuruDurumu, BELGE_DURUM_ETIKETLERI, BelgeDurumu, DURUM_ETIKETLERI } from '../core/models';

const BASVURU_RENK: Record<BasvuruDurumu, string> = {
  TASLAK: 'rozet-gri',
  GONDERILDI: 'rozet-mavi',
  IK_INCELEMESINDE: 'rozet-lacivert',
  EKSIK_BELGE: 'rozet-sari',
  BIRIM_DEGERLENDIRMESINDE: 'rozet-mor',
  ONAYLANDI: 'rozet-yesil',
  REDDEDILDI: 'rozet-kirmizi',
};

const BELGE_RENK: Record<BelgeDurumu, string> = {
  BEKLIYOR: 'rozet-gri',
  ONAYLANDI: 'rozet-yesil',
  REDDEDILDI: 'rozet-kirmizi',
};

@Component({
  selector: 'app-durum-rozet',
  template: `<span class="rozet" [class]="'rozet ' + renk()">{{ etiket() }}</span>`,
})
export class DurumRozet {
  readonly durum = input.required<BasvuruDurumu>();
  readonly renk = computed(() => BASVURU_RENK[this.durum()]);
  readonly etiket = computed(() => DURUM_ETIKETLERI[this.durum()]);
}

@Component({
  selector: 'app-belge-rozet',
  template: `<span [class]="'rozet ' + renk()">{{ etiket() }}</span>`,
})
export class BelgeRozet {
  readonly durum = input.required<BelgeDurumu>();
  readonly renk = computed(() => BELGE_RENK[this.durum()]);
  readonly etiket = computed(() => BELGE_DURUM_ETIKETLERI[this.durum()]);
}
