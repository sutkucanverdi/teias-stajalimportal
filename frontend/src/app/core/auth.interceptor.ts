import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { BildirimService } from './bildirim.service';

/** Her isteğe JWT ekler; oturum süresi dolduysa kullanıcıyı giriş ekranına yönlendirir. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const bildirim = inject(BildirimService);
  const token = auth.token;
  const istek = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(istek).pipe(
    catchError((hata: HttpErrorResponse) => {
      const girisIstegi = req.url.includes('/auth/login');
      if (hata.status === 401 && !girisIstegi && auth.girisYapildi()) {
        auth.cikisYap();
        bildirim.uyari('Oturumunuzun süresi doldu. Lütfen tekrar giriş yapınız.');
      }
      return throwError(() => hata);
    }),
  );
};
