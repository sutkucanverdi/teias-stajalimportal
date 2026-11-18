import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { Rol } from './models';

/** Yalnızca giriş yapmış ve belirtilen rollerden birine sahip kullanıcılar */
export const rolGuard =
  (...roller: Rol[]): CanActivateFn =>
  () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (!auth.girisYapildi() || !auth.token) {
      return router.createUrlTree(['/giris']);
    }
    if (roller.length && !roller.includes(auth.rol()!)) {
      return router.createUrlTree([auth.anaSayfa()]);
    }
    return true;
  };

/** Giriş yapmış kullanıcıyı giriş / kayıt ekranından ana sayfasına gönderir */
export const misafirGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.girisYapildi() && auth.token ? router.createUrlTree([auth.anaSayfa()]) : true;
};
