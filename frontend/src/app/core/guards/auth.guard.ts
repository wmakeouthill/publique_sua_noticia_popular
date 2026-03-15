import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.autenticado()) {
    return true;
  }

  // Verificar se acabou a carga assíncrona. 
  // Na vida real, o guard pode aguardar isLoading se falso. Aqui assumimos que ele tenta verificar a flag.
  if(!authService.carregando() && !authService.autenticado()) {
    return router.parseUrl('/login');
  }

  return true;
};
