import { HttpEvent, HttpHandlerFn, HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, throwError, timer } from 'rxjs';
import { catchError, switchMap, timeout } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

const HTTP_TIMEOUT_MS = 15_000;

export function errorInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
  const auth = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    timeout(HTTP_TIMEOUT_MS),
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        if (error.status === 401 && !req.url.includes('/auth/')) {
          return timer(0).pipe(
            switchMap(() => {
              auth.logout();
              router.navigate(['/login']);
              return throwError(() => error);
            })
          );
        }
      }
      return throwError(() => error);
    })
  );
}
