import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { ROL_ETIKETLERI } from '../core/models';
import { KurumLogo } from '../shared/kurum-logo';

interface MenuOgesi {
  etiket: string;
  yol: string;
}

/** Giriş yapmış kullanıcılar için ana çerçeve: kurumsal üst bant, rol bazlı menü, alt bilgi */
@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, KurumLogo],
  templateUrl: './shell.html',
  styleUrl: './shell.css',
})
export class Shell {
  protected readonly auth = inject(AuthService);
  protected readonly yil = new Date().getFullYear();

  protected readonly rolEtiketi = computed(() => {
    const rol = this.auth.rol();
    return rol ? ROL_ETIKETLERI[rol] : '';
  });

  protected readonly menu = computed<MenuOgesi[]>(() => {
    switch (this.auth.rol()) {
      case 'ROLE_HR':
        return [
          { etiket: 'Gösterge Paneli', yol: '/ik/panel' },
          { etiket: 'Başvurular', yol: '/ik/basvurular' },
          { etiket: 'Birimler', yol: '/ik/birimler' },
          { etiket: 'Belge Türleri', yol: '/ik/belge-turleri' },
          { etiket: 'Kullanıcılar', yol: '/ik/kullanicilar' },
        ];
      case 'ROLE_BIRIM':
        return [{ etiket: 'Birime Gelen Başvurular', yol: '/birim/basvurular' }];
      case 'ROLE_STUDENT':
        return [
          { etiket: 'Başvurum', yol: '/ogrenci/panel' },
          { etiket: 'Birimler ve Kontenjanlar', yol: '/ogrenci/birimler' },
        ];
      default:
        return [];
    }
  });
}
