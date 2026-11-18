import { Routes } from '@angular/router';
import { misafirGuard, rolGuard } from './core/guards';
import { GenelCerceve } from './layout/genel-cerceve';
import { Shell } from './layout/shell';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';

export const routes: Routes = [
  // Giriş / kayıt (oturum açmamış kullanıcılar)
  {
    path: '',
    component: GenelCerceve,
    canActivate: [misafirGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'giris' },
      { path: 'giris', component: Login, title: 'Giriş | TEİAŞ Staj Portalı' },
      { path: 'kayit', component: Register, title: 'Öğrenci Kaydı | TEİAŞ Staj Portalı' },
    ],
  },
  // Eski adreslerle uyumluluk
  { path: 'login', redirectTo: 'giris' },
  { path: 'register', redirectTo: 'kayit' },

  // Oturum açmış kullanıcılar
  {
    path: '',
    component: Shell,
    children: [
      {
        path: 'ogrenci',
        canActivate: [rolGuard('ROLE_STUDENT')],
        children: [
          { path: 'panel', loadComponent: () => import('./pages/ogrenci/ogrenci-panel').then((m) => m.OgrenciPanel), title: 'Başvurum | TEİAŞ Staj Portalı' },
          { path: 'basvuru/yeni', loadComponent: () => import('./pages/ogrenci/basvuru-form').then((m) => m.BasvuruForm), title: 'Yeni Başvuru | TEİAŞ Staj Portalı' },
          { path: 'basvuru/:id/duzenle', loadComponent: () => import('./pages/ogrenci/basvuru-form').then((m) => m.BasvuruForm), title: 'Başvuru Düzenle | TEİAŞ Staj Portalı' },
          { path: 'birimler', loadComponent: () => import('./pages/ogrenci/birimler-listesi').then((m) => m.BirimlerListesi), title: 'Birimler | TEİAŞ Staj Portalı' },
          { path: '', pathMatch: 'full', redirectTo: 'panel' },
        ],
      },
      {
        path: 'ik',
        canActivate: [rolGuard('ROLE_HR')],
        children: [
          { path: 'panel', loadComponent: () => import('./pages/ik/ik-panel').then((m) => m.IkPanel), title: 'Gösterge Paneli | TEİAŞ Staj Portalı' },
          { path: 'basvurular', loadComponent: () => import('./pages/ik/basvuru-listesi').then((m) => m.BasvuruListesi), title: 'Başvurular | TEİAŞ Staj Portalı' },
          { path: 'birimler', loadComponent: () => import('./pages/ik/birim-yonetimi').then((m) => m.BirimYonetimi), title: 'Birimler | TEİAŞ Staj Portalı' },
          { path: 'belge-turleri', loadComponent: () => import('./pages/ik/belge-turu-yonetimi').then((m) => m.BelgeTuruYonetimi), title: 'Belge Türleri | TEİAŞ Staj Portalı' },
          { path: 'kullanicilar', loadComponent: () => import('./pages/ik/kullanici-yonetimi').then((m) => m.KullaniciYonetimi), title: 'Kullanıcılar | TEİAŞ Staj Portalı' },
          { path: '', pathMatch: 'full', redirectTo: 'panel' },
        ],
      },
      {
        path: 'birim',
        canActivate: [rolGuard('ROLE_BIRIM')],
        children: [
          { path: 'basvurular', loadComponent: () => import('./pages/ik/basvuru-listesi').then((m) => m.BasvuruListesi), title: 'Birim Başvuruları | TEİAŞ Staj Portalı' },
          { path: '', pathMatch: 'full', redirectTo: 'basvurular' },
        ],
      },
      {
        path: 'basvuru/:id',
        canActivate: [rolGuard()],
        loadComponent: () => import('./pages/ortak/basvuru-detay').then((m) => m.BasvuruDetay),
        title: 'Başvuru Detayı | TEİAŞ Staj Portalı',
      },
      {
        path: 'profil',
        canActivate: [rolGuard()],
        loadComponent: () => import('./pages/ortak/profil').then((m) => m.Profil),
        title: 'Profilim | TEİAŞ Staj Portalı',
      },
    ],
  },
  { path: '**', redirectTo: 'giris' },
];
