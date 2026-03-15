import { inject } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { CanActivateFn, Router } from '@angular/router';
import { filter, map, take } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.autenticado()) {
    return true;
  }

  return toObservable(authService.carregando).pipe(
    filter((carregando) => !carregando),
    take(1),
    map(() => authService.autenticado() ? true : router.parseUrl('/login'))
  );
};
