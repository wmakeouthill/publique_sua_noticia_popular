import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.autenticado() && auth.usuarioAtual()?.papel === 'ADMIN') {
    return true;
  }

  if (!auth.autenticado()) {
    return router.createUrlTree(['/login']);
  }

  return router.createUrlTree(['/']);
};
